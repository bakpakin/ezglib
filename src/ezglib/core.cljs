(ns ezglib.core)

;;;;; Mode Functions ;;;;;

(defn add-mode!
  "Adds a mode to the game. A mode is a
  function that takes one parameter, game, and updates
  the game. The mode function is called once every time
  through the main loop."
  [game mode-id mode]
  (swap! (:modes game) assoc mode-id mode))

(defn remove-mode!
  "Removes a mode from the game."
  [game mode-id]
  (swap! (:modes game) dissoc mode-id))

(defn current-mode-id
  "Gets the id of the current mode of the game."
  [game]
  @(:mode game))

(defn current-mode
  "Gets the current mode of the game."
  [game]
  (@(:modes game) @(:mode game)))

(defn set-mode!
  "Sets the mode of the game. If mode-id
  does not exist in the game, does nothing and returns nil. Else
  sets the game mode and returns the new mode."
  [game mode-id]
  (if-let [new-mode (@(:modes game) mode-id)]
    (let []
      (reset! (:mode game) mode-id)
      new-mode)
    nil))

(defn modes
  "Gets all availble modes of the game."
  [game]
  @(:modes game))

(defn mode
  "Makes a bare-bones game mode."
  [update-fn]
  {:update (fn [gm]
             (update-fn gm)
             (reset! (:event-queue gm) cljs.core.PersistentQueue.EMPTY))})

;;;;; Events ;;;;;

(defn queue-event!
  "Queues an event that happened in the game."
  [game event-type & params]
  (swap! (:event-queue game) conj [event-type (vec params)]))

(defn event-mode
  "Makes a game mode that gets events from
  the game and executes registered handlers."
  [update-fn]
    (let [handlers (atom {})
          f (fn [gm]
              (update-fn gm)
              (queue-event! gm ::end-update)
              (while (when-let [n (peek @(:event-queue gm))] (not (= ::end-update n)))
                (let [[et params] (peek @(:event-queue gm))]
                  (when-let [hs (@handlers et)]
                    (doseq [h (vals hs)]
                      (apply h params)))
                  (swap! (:event-queue gm) pop))))]
      {:update f
       :handlers handlers
       :handler-types (atom {})
       :next-id (atom 0)}))

(defn add-handler!
  "Adds a handler to the event-mode for a certain event type.
  Returns the handler id."
  [event-mode event-type handler]
  (let [id (swap! (:next-id event-mode) inc)]
    (swap! (:handlers event-mode) assoc-in [event-type id] handler)
    (swap! (:handler-types event-mode) assoc id event-type)
    id))

(defn handler
  "Gets the handler associated with the given id
  in the event-mode."
  [event-mode id]
  (if-let [et (@(:handler-types event-mode) id)]
    (get-in @(:handlers event-mode) [et id])))

(defn remove-handler!
  "Removes a handler from the event-mode by id.
  Returns the handler."
  [event-mode id]
  (if-let [et (@(:handler-types event-mode) id)]
    (let [h (get-in @(:handlers event-mode) [et id])]
      (swap! (:handler-types event-mode) dissoc id)
      (swap! (:handlers event-mode)
         (fn [ets e-t i] (assoc ets e-t (dissoc (ets e-t) i)))
         et
         id)
      h)))

;;;;; Game Initializer Functions ;;;;;

(defn- init-gl!
  "Initializes opengl."
  [canvas]
  (let [gl (or (.getContext canvas "webgl") (.getContext canvas "experimental-webgl"))]
  (if gl
    (let []
      (set! (.-viewportWidth gl) (.-width canvas))
      (set! (.-viewportHeight gl) (.-height canvas))
      (.enable gl (.-DEPTH_TEST gl))
      gl)
    (let []
      (.log js/console "Unable to load webgl context. Your browser may not support it.")
      nil))))

(def ^:private default-mode
   (event-mode
    (fn [game]
     (let [gl (:gl game)]
       (.clearColor gl 0.0 0.0 0.0 1.0)
       (.enable gl (.-DEPTH_TEST gl))
       (.depthFunc gl (.-LEQUAL gl))
       (.clear gl (bit-or (.-COLOR_BUFFER_BIT gl) (.-DEPTH_BUFFER_BIT gl)))))))

(defn game
  "Makes an ezglib game. element-id is the DOM
  element in which the game is injected. game-id is
  the id of the game element."
  [width height element-id game-id]
  (let [e (.getElementById js/document element-id)
        c (.createElement js/document "canvas")
        gl (init-gl! c)
        eq (atom cljs.core.PersistentQueue.EMPTY)
        g {:width width
           :height height
           :modes (atom {:default default-mode})
           :mode (atom :default)
           :element e
           :loop (atom true)
           :canvas c
           :event-queue eq
           :gl gl}]
    (set! (.-id c) game-id)
    (set! (.-width c) width)
    (set! (.-height c) height)
    (.appendChild e c)
    (.addEventListener e "click" (fn [ev] (queue-event! g :click ev)))
    (.addEventListener e "keypress" (fn [ev] (queue-event! g :click ev)))
    g))

(defn- game-loop
  [game mode-id callback-caller]
  (reset! (:mode game) mode-id)
  (reset! (:loop game) true)
  ((fn cb []
      (when @(:loop game) (callback-caller cb))
        (let [s (@(:modes game) @(:mode game))]
          ((:update s) game)))))

(defn main-loop
  "Runs the main loop of a game. If no fps
  is provided, will run at native fps."
  ([game mode-id fps]
   (game-loop game mode-id
              (fn [cb]
                (js/setTimeout cb (/ 1000 fps)))))
  ([game mode-id]
   (game-loop game mode-id js/requestAnimationFrame))
  ([game]
   (main-loop game :default)))

(defn end-game!
  "Ends the main loop of the game."
  [game]
  (reset! (:loop game) false))

;;;;; Asset Management Functions ;;;;;

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
  load-fn can take any number of assets, but free takes one
  parameter. free-fn should take the same parameter that load-fn returns."
  ([asset load-fn free-fn]
    (swap! loaders assoc asset load-fn)
    (swap! releasers assoc asset free-fn)
   nil)
  ([asset load-fn]
   (add-asset asset load-fn (fn [x] nil))))
