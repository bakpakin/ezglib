(ns ezglib.game
  (:require [ezglib.event :as event]
            [ezglib.mode :as mode]
            [ezglib.render :as render]
            [ezglib.sound :as sound]))

(def ^:private default-mode
   (event/event-mode
    (fn [game]
     (render/clear!))))


(defn game
  "Makes an ezglib game. element-id is the DOM
  element in which the game is injected. game-id is
  the id of the game element."
  ([width height element-id game-id]
  (let [e (if-let [tmp (.getElementById js/document element-id)] tmp (.-body js/document))
        c (.createElement js/document "canvas")
        g {:modes (atom {:default default-mode})
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
    g))
  ([width height element-id]
   (game width height element-id ""))
  ([width height]
   (game width height "" "")))

(defn- game-loop!
  [game mode-id callback-caller]
  (reset! (:mode game) mode-id)
  (reset! (:loop game) true)
  ((fn cb []
      (when @(:loop game) (callback-caller cb))
        (let [s (@(:modes game) @(:mode game))]
          ((:update s) game)))))

(defn main-loop!
  "Runs the main loop of a game. If no fps
  is provided, will run at native fps."
  ([game mode-id fps]
   (game-loop! game mode-id
              (fn [cb]
                (js/setTimeout cb (/ 1000 fps)))))
  ([game mode-id]
   (game-loop! game mode-id js/requestAnimationFrame))
  ([game]
   (main-loop! game :default)))

(defn end-game!
  "Ends the main loop of the game."
  [game]
  (reset! (:loop game) false))
