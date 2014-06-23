(ns ezglib.render
  (:require [ezglib.asset :as asset]))

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
      (.clearColor glc 0.0 0.0 0.0 1.0)
      (.enable glc (.-DEPTH_TEST glc))
      (.depthFunc glc (.-LEQUAL glc))
      glc)
    (do
      (.log js/console "Unable to load webgl context. Your browser may not support it.")
      nil)))
    gl)

(defn clear!
  "Clears the gl context."
  []
  (.clear gl (bit-or (.-COLOR_BUFFER_BIT gl) (.-DEPTH_BUFFER_BIT gl))))

(defn- handle-tex-loaded
  [image tex]
  (.bindTexture gl (.-TEXTURE_2D gl) tex)
  (.texImage2D gl (.-TEXTURE_2D gl) 0 (.-RGBA gl) (.-RGBA gl) (.-UNSIGNED_BYTE gl) image)
  (.texParameteri gl (.-TEXTURE_2D gl) (.-TEXTURE_MAG_FILTER gl) (.-LINEAR gl))
  (.texParameteri gl (.-TEXTURE_2D gl) (.-TEXTURE_MIN_FILTER gl) (.-LINEAR_MIPMAP_NEAREST gl))
  (.generateMipmap gl (.-TEXTURE_2D gl))
  (.bindTexture gl (.-TEXTURE_2D gl) nil))

(defn load-texture
  "Loads a texture"
  [url]
  (let [tex (.createTexture gl)
        image (js/Image.)]
    (set! (.-src image) url)
    (set! (.-onload image) (fn [] (handle-tex-loaded image tex)))
    tex))

(defn free-texture
  "Frees a loaded texture"
  [tex]
  (.deleteTexture gl tex))

(asset/add-asset :texture load-texture free-texture)
