(ns ezglib.game
  (:require [ezglib.event :as event]
            [ezglib.sound :as sound]
            [ezglib.input :as input]
            [ezglib.asset :as asset]
            [ezglib.gl :as gl]
            [ezglib.render :as render]
            [ezglib.ecs :as ecs]))

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
  (get @(:modes game) @(:mode game)))

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
      (reset! (:handlers game) @(:handlers new-mode))
      (reset! (:handler-types game) @(:handler-types new-mode))
      new-mode)
    nil))

(defn modes
  "Gets all availble modes of the game."
  [game]
  @(:modes game))

(defn mode
  "Makes a game mode with specified handlers."
  [& {:keys [update render handlers key-press key-release key-down world]}]
  (let [m {:update (or update (fn [] nil))
           :world world
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

(defn game
  "Creates a new game."
  ([width height canvas]
   (let [gl (gl/create-context canvas)
         g {:modes (atom nil)
            :mode (atom nil)
            :width width
            :height height
            :loop (atom true)
            :canvas canvas
            :event-queue (atom cljs.core.PersistentQueue.EMPTY)
            :handlers (atom nil)
            :handler-types (atom nil)
            :gl gl
            :audio-context (sound/create-context)
            :dt (atom 0)
            :now (atom (/ (.getTime (js/Date.)) 1000))}]
     (set! (.-width canvas) width)
     (set! (.-height canvas) height)
     (ezglib.gl/reset-viewport! gl)
     (ezglib.input/init! g)
     (ezglib.gl/clear! gl)
     g))
  ([width height]
   (let [c (.createElement js/document "canvas")]
     (.appendChild (.-body js/document) c)
     (game width height c))))

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

(defn- ^:no-doc update-time
  "Updates the time in the game."
  [game]
  (let [new-now (/ (.getTime (js/Date.)) 1000)
        dt (- new-now @(:now game))]
    (reset! (:now game) new-now)
    (reset! (:dt game) dt)))

(defn- ^:no-doc game-loop!
  [game mode-id callback-caller]
  (reset! (:mode game) mode-id)
  (reset! (:loop game) true)
  ((fn cb []
     (when @(:loop game) (callback-caller cb))
     (let [m (@(:modes game) @(:mode game))]
       (input/enqueue-keys! game)
       (update-time game)
       ((:update m))
       (if (:world m) (ecs/update! (:world m)))
       (event/handle-events! game)))))

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
