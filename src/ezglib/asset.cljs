(ns ezglib.asset)

(def ^:private loaders (atom {}))

(def ^:private is-dones (atom {}))

(def ^:private releasers (atom {}))

(defn asset-group
  "Creates a new asset group."
  []
  {:id-asset (atom {})
   :id-args (atom {})
   ;args-id is a nested map
   :args-id (atom {})})

(def default-group (asset-group))

(defn asset
  "Checks if the given asset has been loaded. If so, returns it - else
  returns nil."
  ([id]
   (asset default-group id))
  ([asset-group id]
    (get @(:id-asset asset-group) id)))

(defn- make-handles
  [args]
  (let [f (fn [[atype id & args]]
               (let [h (apply (@loaders atype) args)
                     isdn (@is-dones atype)
                     akey (vec args)
                     bkey (conj akey atype)]
                 [bkey [h isdn id atype akey]]))
        ret (apply hash-map (mapcat f args))]
    ret))

(defn load!
  "Loads assets async. Returns an atom that encapsulates
  the progress of loading as number between 0 and 1."
  ([& args]
   (let [mp (apply hash-map args)
         type-id-args (or (mp :assets) [])
         on-load (or (mp :on-load) (fn [] nil))
         asset-group (or (mp :asset-group) default-group)
         update (or (mp :update) (fn [p] nil))
         progress (atom 0)
         done-handles (atom 0)
         handles (atom (make-handles type-id-args))
         num-handles (count @handles)
         f (fn cb [ts]
             (update @progress)
             (if (seq @handles)
               (let [finished-handles (select-keys @handles (for [[k [h isdn id atype akey]] @handles :when (isdn h)] k))]
                 (doseq [[bkey [h isdn id atype akey]] finished-handles]
                   (swap! (:args-id asset-group) assoc-in [atype akey] id)
                   (swap! (:id-args asset-group) assoc id bkey)
                   (swap! (:id-asset asset-group) assoc id (isdn h))
                   (.log js/console (str "Loaded " atype " with id " id " from " akey "."))
                   (swap! done-handles inc)
                   (reset! progress (/ @done-handles num-handles)))
                 (reset! handles (apply dissoc @handles (keys finished-handles)))
                 (js/requestAnimationFrame cb))
               (on-load)))]
     (js/requestAnimationFrame f)
     progress)))

(defn free!
  "Frees already loaded assets."
  ([asset-group ids]
   (doseq [id ids]
     (when-let [a (@(:id-asset asset-group) id)]
       (let [bkey (@(:id-args asset-group) id)
             asset-type (peek bkey)
             akey (pop bkey)]
        ((@releasers asset-type) a)
        (swap! (:args-id asset-group) assoc asset-type (dissoc (@(:args-id asset-group) asset-type) akey))
        (swap! (:id-asset asset-group) dissoc id)
        (swap! (:id-args asset-group) dissoc id)
        nil))))
  ([ids]
   (free! default-group ids)))

(defn free-all-type!
  "Frees all assets of a single type."
  ([asset-group asset-type]
   (doseq [id (vals (@(:args-id asset-group) asset-type))]
     (free! asset-group id)))
  ([asset-type]
   (free-all-type! default-group asset-type)))

(defn free-all!
  "Frees all assets of one type, or all
  assets if no type is specified. When called with no
  arguments, will only free the default asset group."
  ([asset-group]
   (doseq [at (keys @(:args-id asset-group))]
     (free-all-type! asset-group at)))
  ([]
   (free-all! default-group)))

(defn add-asset-async
   "Adds functionality for loading and freeing assets async.
  load-fn can take any number of parameters, and shoud return a handle
  to the asset. is-done? is a predicate to check if the loading is done (should take one
  parameter, the asset handle.) If so, should
  return either the original handle or a new handle to the asset. free-fn takes one
  parameter. free-fn should take the same parameter that load-fn returns."
  ([asset load-fn is-done? free-fn]
   (swap! loaders assoc asset load-fn)
   (swap! releasers assoc asset free-fn)
   (swap! is-dones assoc asset is-done?)
   nil)
  ([asset load-fn is-done?]
   (add-asset-async asset load-fn is-done? (fn [x] nil))))

(defn add-asset
  "Adds functionality for loading and freeing assets.
  load-fn can take any number of parameters, but free-fn takes one
  parameter. free-fn should take the same parameter that load-fn returns."
  ([asset load-fn free-fn]
    (add-asset-async asset load-fn identity free-fn))
  ([asset load-fn]
   (add-asset asset load-fn (fn [x] nil))))
