(ns basic.core
  (:require [ezglib.core :as ezcore]))

(def state (ezcore/state
            (fn [game] (.write js/document "Hi."))
            (fn [game] nil)))

(def gm (ezcore/game 600 400 "gameDiv" "game"))

(ezcore/add-state! gm :hi state)

(ezcore/end-game gm)

(ezcore/main-loop gm :hi 1)
