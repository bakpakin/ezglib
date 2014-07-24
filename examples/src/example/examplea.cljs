(ns example.examplea
  (:require [ezglib.game :as game]
            [ezglib.event :as event]
            [ezglib.asset :as asset]
            [ezglib.sound :as sound]
            [ezglib.input :as input]
            [ezglib.util :as util]
            [ezglib.gl :as gl]
            [ezglib.render :as r]))

(declare g sg)

(def g
  (game/create
   :width 800
   :height 600

   :assets [[:sound "beep" "resources/beep.wav"]
            [:sound "coin" "resources/mariocoin.wav"]
            [:texture "star" "resources/star.png"]]

   :modes {:start (game/mode
                   :update (fn [] (r/draw! sg))
                   :key-press {:a #(sound/play (asset/asset "beep"))}
                   :handlers {:click #(sound/play (asset/asset "coin"))})}
   :mode :start))

(def sg (r/graph (:gl g)))

(game/main-loop! g)


