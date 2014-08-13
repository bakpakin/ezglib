(ns ezglib.example.a
  (:require [ezglib.core :as ez]
            [ezglib.math :refer [v add sin*]]))

(defn hello
  []
  (ez/entity
   :velocity (v 5 20)
   :position (v 0 -40)
   :drawable (ez/asset :hi)))

(defn set-path!
  [e]
  (let [vel (ez/prop e :velocity)
        vx (.-x vel)
        vy (.-y vel)
        l2 (+ (* vx vx) (* vy vy))
        l (.sqrt js/Math l2)]
    (ez/set-prop! e :velocity (v (* l (sin* 90)) 0))))

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
              :handlers {:click #(set-path! h)}
              :key-press {:space #(ez/play! (ez/asset :beep))})))

(let [game (ez/game 600 600)]
  (ez/load! game
            :assets [[:sound :beep "assets/beep.wav"]
                     [:text :hi "Arial" 60 "Hello World!"]]
            :on-load (fn []
                       (ez/add-state! game :start (start-state game))
                       (ez/set-state! game :start)
                       (ez/main-loop! game))))
