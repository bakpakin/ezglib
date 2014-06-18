(ns ezglib.asset)

(def ^:private loaders (atom {}))

(def ^:private releasers (atom {}))

(defn asset-group
  "Creates a new asset group."
  []
  {:id-asset (atom {})
   :id-args (atom {})
   ;args-id is a nested map
   :args-id (atom {})})

(def ^:private default-group (asset-group))

(defn asset
  "Checks if the given asset has been loaded. If so, returns it - else
  returns nil."
  ([id]
   (asset default-group id))
  ([asset-group id]
    (get @(:id-asset asset-group) id)))

(defn load!
  "Loads an asset (Prevents multiple loads).
  Will fail if the given id defines an asset already. Returns the new asset."
  ([asset-group asset-type id args]
  (when (not (contains? @(:id-asset asset-group) id))
    (let [akey (vec args)
          bkey (conj akey asset-type)
          new-a (apply (@loaders asset-type) args)]
      (swap! (:args-id asset-group) assoc-in [asset-type akey] id)
      (swap! (:id-args asset-group) assoc id bkey)
      (swap! (:id-asset asset-group) assoc id new-a)
      new-a)))
  ([asset-type id args]
   (load! default-group asset-type id args)))

(defn free!
  "Frees an already loaded asset."
  ([asset-group id]
    (when-let [a (@(:id-asset asset-group) id)]
      (let [bkey (@(:id-args asset-group) id)
            asset-type (peek bkey)
            akey (pop bkey)]
        ((@releasers asset-type) a)
        (swap! (:args-id asset-group) assoc asset-type (dissoc (@(:args-id asset-group) asset-type) akey))
        (swap! (:id-asset asset-group) dissoc id)
        (swap! (:id-args asset-group) dissoc id)
        nil)))
  ([id]
   (free! default-group id)))

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
  "Adds functionality for loading and freeing assets.
  load-fn can take any number of parameters, but free takes one
  parameter. free-fn should take the same parameter that load-fn returns."
  ([asset load-fn free-fn]
    (swap! loaders assoc asset load-fn)
    (swap! releasers assoc asset free-fn)
   nil)
  ([asset load-fn]
   (add-asset asset load-fn (fn [x] nil))))
