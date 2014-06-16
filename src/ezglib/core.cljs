(ns ezglib.core
  (:use [cljs.core.async :only [<! >! chan close! sliding-buffer put! alts! timeout]])
  (:require-macros [cljs.core.async.macros :as asyncm :refer [go alt!]]))

;;;;; State Functions ;;;;;

(defn add-state!
  "Adds a state to the game. A state is a
  function that takes one parameter, game, and updates
  the game."
  [game state-id state]
  (swap! (:states game) assoc state-id state))

(defn remove-state!
  "Removes a state from the game."
  [game state-id]
  (swap! (:states game) dissoc state-id))

(defn current-state
  "Gets the current state of the game."
  [game]
  @(:state game))

(defn set-state!
  "Sets the state of the game. If state-id
  does not exist in the game, does nothing and returns nil. Else
  sets the game state and returns the new state."
  [game state-id]
  (if-let [new-state (@(:states game) state-id)]
    (let []
      (reset! (:state game) state-id)
      new-state)
    nil))

(defn states
  "Gets all availble states of the game."
  [game]
  @(:states game))

(defn default-state
   [game]
   (let [gl (:gl game)]
     (.clearColor gl 0.0 0.0 0.0 1.0)
     (.enable gl (.-DEPTH_TEST gl))
     (.depthFunc gl (.-LEQUAL gl))
     (.clear gl (bit-or (.-COLOR_BUFFER_BIT gl) (.-DEPTH_BUFFER_BIT gl)))))

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

(defn game
  "Makes an ezglib game. element-id is the DOM
  element in which the game is injected. game-id is
  the id of the game element."
  [width height element-id game-id]
  (let [e (.getElementById js/document element-id)
        c (.createElement js/document "canvas")
        gl (init-gl! c)]
    (set! (.-id c) game-id)
    (set! (.-width c) width)
    (set! (.-height c) height)
    (.appendChild e c)

    {:width width
     :height height
     :states (atom {:default default-state})
     :state (atom :default)
     :element e
     :loop (atom true)
     :canvas c
     :gl gl}))

(defn- game-loop
  [game state-id callback-caller]
  (reset! (:state game) state-id)
  (reset! (:loop game) true)
  ((fn cb []
      (when @(:loop game) (callback-caller cb))
        (let [s (@(:states game) @(:state game))]
          (s game)))))

(defn main-loop
  "Runs the main loop of a game. If no fps
  is provided, will run at native fps."
  ([game state-id fps]
   (game-loop game state-id
              (fn [cb]
                (js/setTimeout cb (/ 1000 fps)))))
  ([game state-id]
   (game-loop game state-id js/requestAnimationFrame)))

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

;;;;; Keyboard Functions ;;;;;

