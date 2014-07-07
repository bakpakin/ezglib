(ns example.examplea
  (:require [ezglib.game :as game]
            [ezglib.event :as event]
            [ezglib.asset :as asset]
            [ezglib.sound :as sound]
            [ezglib.input :as input]
            [ezglib.util :as util]
            [ezglib.gl :as gl]))

;Creates the main game object.
(game/init! 800 600)

(defn start
  []

  ;Adds event handlers for click events and keyboard events to the game mode.
  ;This plays the "coin" sound on clicks, and the "beep"
  ;sound when the space key is pressed.
  (let [m (game/current-mode)]
    (event/add-handler! m :click #(sound/play (asset/asset "beep")))
    (input/on-key-press! m :a #(sound/play (asset/asset "coin"))))

  ;Runs the main game loop. To end the loop, call (ezglib.game/end-game!).
  (game/main-loop!))

;Loads two sounds, and stores them as assets named "coin" and "beep".
;When assets are done loading, calls start. This function returns immediatley.
(asset/load!
 :on-load start
 :assets [[:sound "beep" "resources/beep.wav"]
          [:sound "coin" "resources/mariocoin.wav"]
          [:texture "star" "resources/star.png"]])
