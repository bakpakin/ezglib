(ns example.examplea
  (:require [ezglib.game :as game]
            [ezglib.event :as event]
            [ezglib.asset :as asset]
            [ezglib.sound :as sound]
            [ezglib.input :as input]
            [ezglib.util :as util]
            [ezglib.render.gl :as gl]
            [ezglib.render.scene2d :as s2]))

(declare g sg)

(def g
  (game/create
   :width 800
   :height 600
   :mode :start
   :modes {:start (game/mode
                   :update (fn [] (s2/draw! sg))
                   :key-press {:a #(sound/play (asset/asset "beep"))}
                   :handlers {:click #(sound/play (asset/asset "coin"))})}
   :assets [[:sound "beep" "resources/beep.wav"]
            [:sound "coin" "resources/mariocoin.wav"]
            [:texture "star" "resources/star.png"]]))

(def sg (s2/graph (:gl g)))

(game/main-loop! g)


