(ns ezglib.render
  (:require [ezglib.asset :as asset]))

;The WebGl context
(declare gl)

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

(defn- get-shader
  [src frag-vert]
  (let [shader (.createShader gl (case frag-vert :frag (.-FRAGMENT_SHADER gl) :vert (.-VERTEX_SHADER gl)))]
    (.shaderSource gl shader src)
    (.compileShader gl shader)
    (if (not (.getShaderParameter gl shader (.-COMPILE_STATUS gl)))
      (do
        (.log js/console (str "An error occured while compiling the shader: " (.getShaderInfoLog gl shader)))
        nil)
      shader)))

(defn load-shader
  "Loads a texture from vertex and fragment shader."
  [frag-src vert-src]
  (let [frag (get-shader frag-src :frag)
        vert (get-shader vert-src :vert)
        prgrm (.createProgram gl)]
    (.attachShader gl prgrm vert)
    (.attachShader gl prgrm frag)
    (.linkProgram gl prgrm)
    (if (not (.getProgramParameter gl prgrm (.-LINK_STATUS gl)))
      (do
        (.log js/console "An error occured while linking the shader.")
        nil)
      prgrm)))

(defn use-shader
  "Bind a shader to gl context."
  [shader-program]
  (.useProgram gl shader-program))

(defn shader-attr
  "Gets a shader attribute location"
  [shader attr]
  (.getAttribLocation gl shader attr))

(def ^:private default-frag-src
  "void main(void) {
    gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
  }"
  )

(def ^:private default-vert-src
  "attribute vec3 aVertexPosition;

  uniform mat4 uMVMatrix;
  uniform mat4 uPMatrix;

  void main(void) {
    gl_Position = uPMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);
  }"
  )

(declare default-shader)

(defn init!
  "Initializes opengl. Returns gl context."
  [canvas]
  (def gl (if-let [glc (or (.getContext canvas "webgl") (.getContext canvas "experimental-webgl"))]
    (do
      (set! (.-viewportWidth glc) (.-width canvas))
      (set! (.-viewportHeight glc) (.-height canvas))
      (.enable glc (.-DEPTH_TEST glc))
      (.clearColor glc 0.0 0.0 0.0 1.0)
      (.enable glc (.-DEPTH_TEST glc))
      (.depthFunc glc (.-LEQUAL glc))
      (def default-shader (load-shader default-frag-src default-vert-src))
      glc)
    (do
      (.log js/console "Unable to load webgl context. Your browser may not support it.")
      nil)))
    gl)
