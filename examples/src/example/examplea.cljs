(ns example.examplea
  (:require [ezglib.game :as game]
            [ezglib.event :as event]
            [ezglib.mode :as mode]
            [ezglib.asset :as asset]
            [ezglib.sound :as sound]))

(def gm (game/game 600 400 "gameDiv" "game"))

(def em (mode/current-mode gm))

(asset/load! :sound "coin" [gm "resources/mariocoin.wav"])

(event/add-handler! em :click #(sound/play (asset/asset "coin")))
(event/add-handler! em :key #(js/alert "Key."))

(game/main-loop! gm)
