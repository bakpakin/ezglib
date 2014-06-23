(ns example.examplea
  (:require [ezglib.game :as game]
            [ezglib.event :as event]
            [ezglib.asset :as asset]
            [ezglib.sound :as sound]
            [ezglib.input :as input]))

;Creates the main game object.
(game/init! 600 400)

;Loads two sounds, and stores them as assets named "coin" and "beep".
;This is asynchronous, and returns immediatly.
(asset/load! [[:sound "coin" "resources/mariocoin.wav"]
              [:sound "beep" "resources/beep.wav"]])

;Defines em as the current game mode. Game modes are what you use
;to define different game modes or states, like a title screen and different levels.
(def m (game/current-mode))

;Adds event handlers for click events and keyboard events to the game mode.
;The default current-mode is an event-mode, so you can add handlers to it to
;listen for events. This plays the "coin" sound on clicks, and the "beep"
;sound when the space key is pressed.
(event/add-handler! m :click #(sound/play (asset/asset "coin")))
(input/on-key-press! m :a #(sound/play (asset/asset "beep")))

;Runs the main game loop. To end the loop, call (ezglib.game/end-game!).
(game/main-loop!)
