(ns ezglib.game
  (:require [ezglib.event :as event]
            [ezglib.render :as render]
            [ezglib.sound :as sound]
            [ezglib.input :as input]))

(declare ^:private game)

(declare mode)

(defn- default-mode
  []
   (mode
    (fn [game]
     (render/clear!))))

(defn- make-game
  "Makes an ezglib game. element-id is the DOM
  element in which the game is injected. game-id is
  the id of the game element."
  ([width height element-id game-id]
  (let [e (if-let [tmp (.getElementById js/document element-id)] tmp (.-body js/document))
        c (.createElement js/document "canvas")
        g {:modes (atom {:default (default-mode)})
           :mode (atom :default)
           :element e
           :loop (atom true)
           :canvas c}]
    (.appendChild e c)
    (set! (.-id c) game-id)
    (set! (.-width c) width)
    (set! (.-height c) height)
    (render/init! c)
    (sound/init!)
    (event/init! c)
    (input/init! c)
    g))
  ([width height element-id]
   (make-game width height element-id ""))
  ([width height]
   (make-game width height "" "")))

(defn canvas
  "Gets the game canvas used for rendering."
  []
  (:cnvas game))

(defn init!
  "Initializes the ezglib game. element-id is the DOM
  element in which the game is injected. game-id is
  the id of the game element."
  ([w h element-id game-id]
   (def ^:private game (make-game w h element-id game-id)))
  ([w h element-id]
   (def ^:private game (make-game w h element-id)))
  ([w h]
   (def ^:private game (make-game w h))))

(defn- game-loop!
  [mode-id callback-caller]
  (reset! (:mode game) mode-id)
  (reset! (:loop game) true)
  ((fn cb []
      (when @(:loop game) (callback-caller cb))
        (let [s (@(:modes game) @(:mode game))]
          (input/enqueue-keys!)
          ((:update s))
          (event/handle-events! s)))))

(defn main-loop!
  "Runs the main loop of a game. If no fps
  is provided, will run at native fps."
  ([mode-id fps]
   (game-loop! mode-id
              (fn [cb]
                (js/setTimeout cb (/ 1000 fps)))))
  ([mode-id]
   (game-loop! mode-id js/requestAnimationFrame))
  ([]
   (main-loop! :default)))

(defn end-game!
  "Ends the main loop of the game."
  []
  (reset! (:loop game) false))

;;;;; Mode Functions ;;;;;

(defn add-mode!
  "Adds a mode to the game. A mode is a
  function that takes one parameter, game, and updates
  the game. The mode function is called once every time
  through the main loop."
  [mode-id mode]
  (swap! (:modes game) assoc mode-id mode))

(defn remove-mode!
  "Removes a mode from the game."
  [mode-id]
  (swap! (:modes game) dissoc mode-id))

(defn current-mode-id
  "Gets the id of the current mode of the game."
  []
  @(:mode game))

(defn current-mode
  "Gets the current mode of the game."
  []
  (@(:modes game) @(:mode game)))

(defn set-mode!
  "Sets the mode of the game. If mode-id
  does not exist in the game, does nothing and returns nil. Else
  sets the game mode and returns the new mode."
  [mode-id]
  (if-let [new-mode (@(:modes game) mode-id)]
    (do
      (reset! (:mode game) mode-id)
      new-mode)
    nil))

(defn modes
  "Gets all availble modes of the game."
  []
  @(:modes game))

(defn mode
  "Makes a bare-bones game mode."
  [update-fn]
  {:update update-fn
   :handlers (atom {})
   :handler-types (atom {})
   :next-id (atom 0)})