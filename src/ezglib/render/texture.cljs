(ns ezglib.render.texture
  (:require [ezglib.asset :as asset]
            [ezglib.render.gl :as gl]))

(defn- handle-tex-loaded
  [gl image tex]
  (.bindTexture gl gl/texture-2d tex)
  (.texImage2D gl gl/texture-2d 0 gl/rgba gl/rgba gl/unsigned-byte image)
  (.texParameteri gl gl/texture-2d gl/texture-mag-filter gl/linear)
  (.texParameteri gl gl/texture-2d gl/texture-min-filter gl/linear-mipmap-nearest)
  (.generateMipmap gl gl/texture-2d)
  (.bindTexture gl gl/texture-2d nil))

(defn- is-tex-loaded?
  [game [tex atm]]
  (when @atm tex))

(defn- load-texture
  "Loads a texture."
  [game url]
  (let [gl (:gl game)
        tex (.createTexture gl)
        image (js/Image.)
        atm (atom false)]
    (set! (.-src image)  url)
    (set! (.-onload image) (fn []
                             (handle-tex-loaded (:gl game) image tex)
                             (reset! atm true)))
    [tex atm]))

(defn- free-texture
  "Frees a loaded texture"
  [game tex]
  (.deleteTexture (:gl game) tex))

(asset/add-asset
 :asset :texture
 :load-fn load-texture
 :is-done? is-tex-loaded?
 :free-fn free-texture)
