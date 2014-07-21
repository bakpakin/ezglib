(ns ezglib.render.scene2d
  (:require [ezglib.render.gl :as gl]))

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
    (gl/clear-color gl 0.3 0.8 0.8 1.0)))
