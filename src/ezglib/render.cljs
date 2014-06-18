(ns ezglib.render)

;The WebGl context
(declare ^:private gl)

(defn init!
  "Initializes opengl. Returns gl context."
  [canvas]
  (def ^:private gl (if-let [glc (or (.getContext canvas "webgl") (.getContext canvas "experimental-webgl"))]
    (do
      (set! (.-viewportWidth glc) (.-width canvas))
      (set! (.-viewportHeight glc) (.-height canvas))
      (.enable glc (.-DEPTH_TEST glc))
      glc)
    (do
      (.log js/console "Unable to load webgl context. Your browser may not support it.")
      nil)))
    gl)

(defn clear!
  "Clears the gl context."
  []
  (.clearColor gl 0.0 0.0 0.0 1.0)
  (.enable gl (.-DEPTH_TEST gl))
  (.depthFunc gl (.-LEQUAL gl))
  (.clear gl (bit-or (.-COLOR_BUFFER_BIT gl) (.-DEPTH_BUFFER_BIT gl))))
