(ns ezglib.game
  (:require [ezglib.event :as event]
            [ezglib.sound :as sound]
            [ezglib.input :as input]
            [ezglib.asset :as asset]
            [ezglib.render.gl :as gl]
            ;require shader and texture so they are loaded into compiler.
            [ezglib.render.shader :as shader]
            [ezglib.render.texture :as texture]))

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

(defn get-mode
  "Retrieves a mode by id from the game."
  [game id]
  (@(:modes game) id))

(defn set-mode!
  "Sets the mode of the game. If mode-id
  does not exist in the game, does nothing and returns nil. Else
  sets the game mode and returns the new mode."
  [game mode-id]
  (if-let [new-mode (@(:modes game) mode-id)]
    (do
      (reset! (:mode game) mode-id)
      new-mode)
    nil))

(defn modes
  "Gets all availble modes of the game."
  [game]
  @(:modes game))

(defn mode
  "Makes a game mode with specified handlers."
  [& {:keys [update handlers key-press key-release key-down]}]
  (let [m {:update (or update (fn [] nil))
           :handlers (atom {})
           :handler-types (atom {})
           :next-id (atom 0)}]
    (doseq [[k v] handlers]
      (event/add-handler! m k v))
    (doseq [[k v] key-press]
      (input/on-key-press! m k v))
    (doseq [[k v] key-release]
      (input/on-key-release! m k v))
    (doseq [[k v] key-down]
      (input/on-key-down! m k v))
    m))

;;;;; GAME FUNCTIONS ;;;;;

(declare main-loop!)

(defn create
  "Makes an ezglib game. element-id is the DOM
  element in which the game is injected. game-id is
  the id of the game element."
  [& {:keys [width height element element-id game-id modes mode canvas
             assets on-load load-update start-on-load?]}]
  (let [e (if-let [tmp (.getElementById js/document element-id)] tmp (.-body js/document))
        c (.createElement js/document "canvas")
        g {:modes (atom (or modes {:default (mode)}))
           :mode (atom (or mode :default))
           :element (or element e)
           :loop (atom true)
           :canvas (or canvas c)
           :event-queue (atom cljs.core.PersistentQueue.EMPTY)
           :gl (gl/create-context c)
           :audio-context (sound/create-context)
           :dt (atom 0)
           :now (atom (.getTime (js/Date.)))}]
    (.appendChild e c)
    (when game-id
      (set! (.-id c) game-id))
    (set! (.-width c) width)
    (set! (.-height c) height)
    (input/init! g)
    (gl/clear! (:gl g))
    (when assets
      (asset/load!
        :game g
        :assets assets
        :update load-update
        :on-load (if start-on-load?
                   (if on-load
                     (fn []
                       (on-load)
                       (main-loop! g))
                     #(main-loop! g))
                   on-load)))
    g))

(defn canvas
  "Gets the game canvas used for rendering."
  [game]
  (:canvas game))

(defn delta
  "Gets the change in time since the last frame."
  [game]
  @(:dt game))

(defn now
  "Gets the time of the current frame."
  [game]
  @(:now game))

(defn event-queue
  "Gets the event queue of the game."
  [game]
  @(:event-queue game))

(defn gl
  "Gets the gl context from the game."
  [game]
  (:gl game))

(defn audio-context
  "Gets the audio context from the game."
  [game]
  (:audio-context game))

(defn- update-time
  "Updates the time in the game."
  [game]
  (let [new-now (.getTime (js/Date.))
        dt (- new-now (:now game))]
    (reset! (:now game) new-now)
    (reset! (:dt game) dt)))

(defn- game-loop!
  [game mode-id callback-caller]
  (reset! (:mode game) mode-id)
  (reset! (:loop game) true)
  ((fn cb []
      (when @(:loop game) (callback-caller cb))
        (let [m (@(:modes game) @(:mode game))]
          (input/enqueue-keys! game)
          (update-time game)
          ((:update m))
          (event/handle-events! game m)))))

(defn main-loop!
  "Runs the main loop of a game. If no fps
  is provided, will run at native fps."
  ([game fps]
   (game-loop! game @(:mode game)
              (fn [cb]
                (js/setTimeout cb (/ 1000 fps)))))
  ([game]
   (game-loop! game @(:mode game) js/requestAnimationFrame)))

(defn end-game!
  "Ends the main loop of the game."
  [game]
  (reset! (:loop game) false))
