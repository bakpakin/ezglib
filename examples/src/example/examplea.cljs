(ns example.examplea
  (:require [ezglib.core :as ezg]))

(def gm (ezg/game 600 400 "gameDiv" "game"))

(def em (ezg/current-mode gm))

(ezg/add-handler! em :click #(js/alert "Click."))
(ezg/add-handler! em :key #(js/alert "Key."))

(ezg/main-loop gm)
