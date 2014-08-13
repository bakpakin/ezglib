(ns ezglib.example.a
  (:require [ezglib.core :as ez]))

(defn hello
  []
  (ez/entity :drawable (ez/asset :hi)))

(defn start-state
  [game]
  (let [w (ez/world
           (ez/render-system game)
           (hello))]
    (ez/state game
              :world w
              :key-press {:space #(ez/play! (ez/asset :beep))})))

(let [game (ez/game 600 600)]
  (ez/load! game
            :assets [[:sound :beep "assets/beep.wav"]
                     [:text :hi "Arial" 60 "Hello World!"]]
            :on-load (fn []
                       (ez/add-state! game :start (start-state game))
                       (ez/set-state! game :start)
                       (ez/main-loop! game))))
