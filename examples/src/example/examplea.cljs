(ns example.examplea
  (:require [ezglib.game :as game]
            [ezglib.event :as event]
            [ezglib.asset :as asset]
            [ezglib.sound :as sound]
            [ezglib.input :as input]
            [ezglib.util :as util]
            [ezglib.render.gl :as gl]))

(declare g)

(def g
  (game/create
   :width 800
   :height 600
   :mode :start
   :modes {:start (game/mode
                   :update (fn []
                             (gl/clear-color (:gl g) 1 .8 .7 1))
                   :key-press {:a #(sound/play (asset/asset "beep"))
                               :r #(sound/play (asset/asset "roar"))
                               :d #(sound/play (asset/asset "drops"))}
                   :handlers {:click #(sound/play (asset/asset "coin"))})}
   :assets [[:sound "beep" "resources/beep.wav"]
            [:sound "coin" "resources/mariocoin.wav"]
            [:sound "roar" "resources/Roar.mp3"]
            [:sound "drops" "resources/Drops of Jupiter.mp3"]
            [:texture "star" "resources/star.png"]]
   :start-on-load? true))


