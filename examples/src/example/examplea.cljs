(ns example.examplea
  (:require [ezglib.game :as game]
            [ezglib.event :as event]
            [ezglib.mode :as mode]
            [ezglib.asset :as asset]))

(def gm (game/game 600 400 "gameDiv" "game"))

(def em (mode/current-mode gm))

(event/add-handler! em :click #(js/alert "Click."))
(event/add-handler! em :key #(js/alert "Key."))

(game/main-loop! gm)
