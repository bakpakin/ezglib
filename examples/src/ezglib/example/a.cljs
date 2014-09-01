(ns ezglib.example.a
  (:require [ezglib.core :as ez]
            [ezglib.util :as util :refer [log]]
            [ezglib.math :refer [v add sin* cos* atan2*]]))

(defn hello
  []
  (ez/entity
   :position (v 0 0)
   :velocity (v 4 20)
   :drawable (ez/asset :hi)))

(defn start-state
  [game]
  (let [h (hello)
        w (ez/world
           (ez/movement-system game)
           (ez/transform2d-system game)
           (ez/render-system game)
           h)]
    (ez/state game
              :world w
              :key-press {:space #(ez/play! (ez/asset :beep))})))

(let [game (ez/game 400 400)]
  (ez/load! game
            :assets {:beep [:sound "assets/beep.wav"]
                     :hi [:text "Arial" 60 "Hello World!"]}
            :on-load (fn []
                       (ez/add-state! game :start (start-state game))
                       (ez/set-state! game :start)
                       (ez/main-loop! game))))
