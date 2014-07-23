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
                   :data (gl/float32 [1.0 1.0 0.0
                                      -1.0 1.0 0.0
                                      1.0 -1.0 0.0
                                      -1.0 -1.0 0.0])
                   :item-size 3)
   :uv-buffer (gl/buffer
               :gl gl
               :data (gl/float32 [0.0 0.0
                                  0.0 1.0
                                  1.0 0.0
                                  1.0 1.0])
               :item-size 2)
   :element-buffer (gl/buffer
                    :gl gl
                    :target gl/element-array-buffer
                    :data (gl/uint16 [0 1 2 1 2 3]))})

(defn draw!
  "Draws the scenegraph."
  [graph]
  (let [{:keys [gl texture-shader color-shader vertex-buffer element-buffer uv-buffer]} graph]
    (gl/clear-color gl 0.3 0.8 0.8 1.0)
    (gl/draw! gl
              :shader texture-shader
              :draw-mode gl/triangles
              :count 6

              :attributes
              {:position {:buffer vertex-buffer}
               :aUv {:buffer uv-buffer}}

              :textures
              {0 (asset/asset "star")}

              :uniforms
              {:color (gl/float32 [1.0 1.0 1.0 1.0])
               :tDiffuse 0
               :projectionMatrix (m/ortho -4 4 -3 3 -1 1)
               :modelViewMatrix (m/rotate-z 5)}

              :element-array
              {:buffer element-buffer
               :count 6
               :offset 0})))
