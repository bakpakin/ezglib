(ns ezglib.example.a
  (:require [ezglib.core :as ez]
            [ezglib.util :as util :refer [log]]
            [ezglib.math :refer [v add sin* cos* atan2*]]))

(defn hello
  []
  (ez/entity
   :velocity (v 0 120)
   :position (v 0 0)
   :drawable (ez/asset :hi)))

(defn set-path!
  [game e]
  (let [[vx vy] (ez/prop e :velocity)
        [px py] (ez/prop e :position)
        [mx my] (ez/mouse-pos game)
        l2 (+ (* vx vx) (* vy vy))
        l (.sqrt js/Math l2)
        d (atan2* (- my py) (- mx px))]
    (ez/set-prop! e :velocity (v (* l (cos* d)) (* l (sin* d))))))

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
              :handlers {:mouse-down #(set-path! game h)}
              :key-press {:space #(ez/play! (ez/asset :beep))})))

(let [game (ez/game 600 600)]
  (ez/load! game
            :assets [[:sound :beep "assets/beep.wav"]
                     [:text :hi "Arial" 60 "Hello World!"]]
            :on-load (fn []
                       (ez/add-state! game :start (start-state game))
                       (ez/set-state! game :start)
                       (ez/main-loop! game))))
