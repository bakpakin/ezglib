(ns ezglib.asset
  (:require [ezglib.util :as util]))

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
    (get (get @(:id-asset asset-group) id) 1)))

(defn- make-handles
  [game args]
  (let [f (fn [[atype id & args]]
               (let [h (apply (@loaders atype) game args)
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
         game (or (mp :game) nil)
         progress (atom 0)
         done-handles (atom 0)
         handles (atom (make-handles game type-id-args))
         num-handles (count @handles)
         f (fn cb [ts]
             (update @progress)
             (if (seq @handles)
               (let [finished-handles (select-keys @handles (for [[k [h isdn id atype akey]] @handles :when (isdn game h)] k))]
                 (doseq [[bkey [h isdn id atype akey]] finished-handles]
                   (swap! (:args-id asset-group) assoc-in [atype akey] id)
                   (swap! (:id-args asset-group) assoc id bkey)
                   (swap! (:id-asset asset-group) assoc id [game (isdn game h)])
                   (util/log "Loaded " (name atype) " with id \"" id "\" from " akey ".")
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
     (when-let [[game a] (@(:id-asset asset-group) id)]
       (let [bkey (@(:id-args asset-group) id)
             asset-type (peek bkey)
             akey (pop bkey)]
        ((@releasers asset-type) game a)
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

(defn add-asset
  "Adds functionality for loading and freeing assets. Takes named parameters.
  \n
  :asset - the name of the asset to define, should be a keyword.\n
  :load-fn - fn that takes at least one argument, the game, and any number of other arguments.
  Returns a handle to the loading process.\n
  :is-done? (optional) - fn to check if the loading is done (should take two
  parameters, the game and the asset handle.)
  If so, should return either the original handle or a new handle to the asset.
  Default value is (fn [game x] x).\n
  :free-fn (optional) - fn that frees the asset from memory (takes two
  parameters, the game and the asset handle).
  free-fn should take the same parameter that is-fn returns.
  Default value is (fn [game x] nil)."
  [& {:keys [asset load-fn is-done? free-fn]}]
  (swap! loaders assoc asset load-fn)
  (swap! releasers assoc asset (or free-fn (fn [game x] nil)))
  (swap! is-dones assoc asset (or is-done? (fn [game x] x)))
  nil)
