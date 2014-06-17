(ns ezglib.render)

(defn clear!
  "Clears the gl context."
  [gl]
  (.clearColor gl 0.0 0.0 0.0 1.0)
  (.enable gl (.-DEPTH_TEST gl))
  (.depthFunc gl (.-LEQUAL gl))
  (.clear gl (bit-or (.-COLOR_BUFFER_BIT gl) (.-DEPTH_BUFFER_BIT gl))))

(defn init-gl!
  "Initializes opengl."
  [canvas]
  (let [gl (or (.getContext canvas "webgl") (.getContext canvas "experimental-webgl"))]
  (if gl
    (let []
      (set! (.-viewportWidth gl) (.-width canvas))
      (set! (.-viewportHeight gl) (.-height canvas))
      (.enable gl (.-DEPTH_TEST gl))
      gl)
    (let []
      (.log js/console "Unable to load webgl context. Your browser may not support it.")
      nil))))
