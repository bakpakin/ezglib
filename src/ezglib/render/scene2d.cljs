(ns ezglib.render.scene2d
  (:require [ezglib.render.gl :as gl]
            [ezglib.util :as util]
            [ezglib.asset :as asset]
            [ezglib.matrix :as m]))

;;;;; PROTOCOLS ;;;;;

(defprotocol ICamera
  (-matrix [this]))

(defprotocol IGraph
  (-draw! [this]))

(defn graph
  "Creates a new 2d scene graph."
  [gl]
  {:gl gl
   :texture-shader (gl/texture-shader gl)
   :color-shader (gl/color-shader gl)
   :vertex-buffer (gl/buffer
                   :gl gl
                   :data (gl/float32 [1.0 1.0 1.0
                                      -1.0 1.0 1.0
                                      1.0 -1.0 1.0
                                      -1.0 -1.0 1.0
                                      1.0 1.0 -1.0
                                      -1.0 1.0 -1.0
                                      1.0 -1.0 -1.0
                                      -1.0 -1.0 -1.0])
                   :item-size 3)
   :uv-buffer (gl/buffer
               :gl gl
               :data (gl/float32 [0.0 0.0
                                  0.0 1.0
                                  1.0 0.0
                                  1.0 1.0
                                  1.0 1.0
                                  1.0 0.0
                                  0.0 1.0
                                  0.0 0.0])
               :item-size 2)
   :element-buffer (gl/buffer
                    :gl gl
                    :target gl/element-array-buffer
                    :data (gl/uint16 [0 1 2 1 2 3
                                      4 5 6 5 6 7
                                      0 1 4 1 4 5
                                      0 2 4 2 4 6
                                      1 3 5 3 5 7
                                      2 3 6 3 6 7]))})

(defn draw!
  "Draws the scenegraph."
  [graph]
  (let [{:keys [gl texture-shader color-shader vertex-buffer element-buffer uv-buffer]} graph]
    (gl/clear-color gl 0.3 0.8 0.8 1.0)
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
              {:color (gl/float32 [1.0 1.0 1.0 1.0])
               :tDiffuse 0
               :projectionMatrix (m/perspective 90 (/ 4 3) 0.01 100)
               :modelViewMatrix (m/mult
                                 (m/rotate-y (/ (util/now) 20))
                                 (m/rotate-x (/ (util/now) 70))
                                 (m/translate 0 0 5))}

              :element-array
              {:buffer element-buffer
               :offset 0})))
