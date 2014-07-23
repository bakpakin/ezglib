(ns ezglib.render.scene2d
  (:require [ezglib.render.gl :as gl]
            [ezglib.util :as util]
            [ezglib.matrix :as m]))

(defn graph
  "Creates a new 2d scene graph."
  [gl]
  {:gl gl
   :texture-shader (gl/texture-shader gl)
   :color-shader (gl/simple-color-shader gl)
   :vertex-buffer (gl/buffer
                   :gl gl
                   :data (gl/float32 [1.0 1.0 0.0
                                      -1.0 1.0 0.0
                                      1.0 -1.0 0.0])
                   :item-size 3)
   :element-buffer (gl/buffer
                    :gl gl
                    :target gl/element-array-buffer
                    :data (gl/uint16 [0 1 2]))})

(defn draw!
  "Draws the scenegraph."
  [graph]
  (let [{:keys [gl texture-shader color-shader vertex-buffer element-buffer]} graph]
    (gl/clear-color gl 0.3 0.8 0.8 1.0)
    (gl/draw! gl
              :shader color-shader
              :draw-mode gl/triangles
              :count 3

              :attributes
              {:position
               {:buffer vertex-buffer}}

              :uniforms
              {:color (gl/float32 [0.5 0.6 0.7 1.0])
               :projectionMatrix (m/ortho -10 10 -10 10 -1 1)
               :modelViewMatrix (m/i)}

              :element-array
              {:buffer element-buffer
               :count 3
               :offset 0})))
