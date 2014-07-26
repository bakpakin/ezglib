(ns example.examplea
  (:require [ezglib.game :as game]
            [ezglib.event :as event]
            [ezglib.asset :as asset]
            [ezglib.sound :as sound]
            [ezglib.input :as input]
            [ezglib.util :as util]
            [ezglib.math :as m]
            [ezglib.gl :as gl]
            [ezglib.render :as r]))

(declare g gl texture-shader vertex-buffer element-buffer uv-buffer text)

(defn draw!
  "Draws the cube."
  []
  (gl/clear! gl)
  (gl/draw! gl
            :shader texture-shader
            :draw-mode gl/triangles
            :count 36

            :attributes
            {:position {:buffer vertex-buffer}
            :aUv {:buffer uv-buffer}}

            :textures
            {0 (asset/asset "star")}

            :uniforms
            {:color (util/float32 [1.0 1.0 1.0 1.0])
             :tDiffuse 0
             :projectionMatrix (m/m-perspective 60 (gl/native-aspect gl) 0.01 100)
             :modelViewMatrix (m/*
                               (m/m-rotate-y (/ (util/now) 20))
                               (m/m-translate 0 0 5))}
            :element-buffer element-buffer))

(def g
  (game/create
   :width 500
   :height 500

   :preload (fn [game]
              (def gl (:gl game))
              (def texture-shader (r/texture-shader gl))
              (def vertex-buffer (gl/buffer
                                  gl
                                  :data (util/float32 [1.0 1.0 1.0
                                                       -1.0 1.0 1.0
                                                       1.0 -1.0 1.0
                                                       -1.0 -1.0 1.0
                                                       1.0 1.0 -1.0
                                                       -1.0 1.0 -1.0
                                                       1.0 -1.0 -1.0
                                                       -1.0 -1.0 -1.0])
                                  :item-size 3))
              (def uv-buffer (gl/buffer
                              gl
                              :data (util/float32 [0.0 0.0
                                                   0.0 1.0
                                                   1.0 0.0
                                                   1.0 1.0
                                                   0.0 1.0
                                                   0.0 0.0
                                                   1.0 1.0
                                                   1.0 0.0])
                              :item-size 2))
              (def element-buffer (gl/buffer
                                   gl
                                   :target gl/element-array-buffer
                                   :data (util/uint16 [0 1 2 1 2 3
                                                       4 5 6 5 6 7
                                                       0 1 4 1 4 5
                                                       0 2 4 2 4 6
                                                       1 3 5 3 5 7
                                                       2 3 6 3 6 7]))))

   :assets [[:sound "beep" "resources/beep.wav"]
            [:sound "coin" "resources/mariocoin.wav"]
            [:texture "star" "resources/star.png"]
            [:text "text" "Arial" 52 "Happy!"]]

   :modes {:start (game/mode
                   :update (fn [] (draw!))
                   :key-press {:a #(sound/play (asset/asset "beep"))}
                   :handlers {:click #(sound/play (asset/asset "coin"))})}
   :mode :start
   :start-on-load? true))
