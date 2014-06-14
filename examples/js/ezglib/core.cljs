(ns ezglib.core)

(defn state
  "Creates a new game state. A game state has two components:
  an update function and a render function. Both functions are called
  once a frame."
  [update render]
  [update render])

(defn add-state!
  "Adds a state to the game."
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

(defn states
  "Gets alll availble states of the game."
  [game]
  @(:states game))

(def default-state
  (state
   (fn [game] nil)
   (fn [game] nil)))

(defn game
  "Makes an ezglib game. element-id is the DOM
  element in which the game is injected. game-id is
  the id of the game element."
  [width height element-id game-id]
  (let [e (.getElementById js/document element-id)
        c (.createElement js/document "canvas")]
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
     :canvas c}))

(defn- game-loop
  [game state-id callback-caller]
  (reset! (:state game) state-id)
  (reset! (:loop game) true)
  ((fn cb []
      (when @(:loop game) (callback-caller cb))
        (let [[u r] (@(:states game) @(:state game))]
          (u game)
          (r game)))))

(defn main-loop
  "Runs the main loop of a game. If no fps
  is provided, will run at native fps."
  ([game state-id fps]
   (game-loop game state-id
              (fn [cb]
                (js/setTimeout cb (/ 1000 fps)))))
  ([game state-id]
   (game-loop game state-id js/requestAnimationFrame)))

(defn end-game
  "Ends the main loop of the game."
  [game]
  (reset! (:loop game) false))
