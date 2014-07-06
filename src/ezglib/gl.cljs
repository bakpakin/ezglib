(ns ezglib.gl
  (:require [ezglib.asset :as asset]
            [ezglib.util :as util]
            [goog.math.Matrix :as mat]
            [goog.math.Vec3 :as v3]
            [goog.math.Vec2 :as v2]))

;The WebGl context
(declare gl)

(defn clear!
  "Clears the gl context."
  []
  (.clear gl (bit-or (.-COLOR_BUFFER_BIT gl) (.-DEPTH_BUFFER_BIT gl))))

;;;;; TEXTURE FUNCTIONS ;;;;;

(defn- handle-tex-loaded
  [image tex]
  (.bindTexture gl (.-TEXTURE_2D gl) tex)
  (.texImage2D gl (.-TEXTURE_2D gl) 0 (.-RGBA gl) (.-RGBA gl) (.-UNSIGNED_BYTE gl) image)
  (.texParameteri gl (.-TEXTURE_2D gl) (.-TEXTURE_MAG_FILTER gl) (.-LINEAR gl))
  (.texParameteri gl (.-TEXTURE_2D gl) (.-TEXTURE_MIN_FILTER gl) (.-LINEAR_MIPMAP_NEAREST gl))
  (.generateMipmap gl (.-TEXTURE_2D gl))
  (.bindTexture gl (.-TEXTURE_2D gl) nil))

(defn- is-tex-loaded?
  [[tex atm]]
  (when @atm tex))

(defn- load-texture
  "Loads a texture."
  [url]
  (let [tex (.createTexture gl)
        image (js/Image.)
        atm (atom false)]
    (set! (.-src image) url)
    (set! (.-onload image) (fn []
                             (handle-tex-loaded image tex)
                             (reset! atm true)))
    [tex atm]))

(defn- free-texture
  "Frees a loaded texture"
  [tex]
  (.deleteTexture gl tex))

(asset/add-asset-async :texture load-texture is-tex-loaded? free-texture)

;;;;; SHADER FUNCTIONS ;;;;;

(def ^:dynamic *program* nil)

(def frag-header
  "
  precision mediump float;\n
  varying vec2 vUv;\n
  uniform sampler2D tDiffuse;\n
  uniform sampler2D tNormal;\n
  uniform vec4 color;\n
  ")

(def default-frag-src
  "void main(void) {\n
    gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);\n
  }"
  )

(def vert-header
  "
  precision mediump float;\n
  attribute vec3 position;\n
  attribute vec2 aUv;\n
  varying vec2 vUv;\n
  uniform mat4 projectionMatrix;\n
  uniform mat4 modelViewMatrix;\n
  ")

(def default-vert-src
  "void main(void) {\n
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);\n
    vUv = aUv;\n
  }"
  )

(defn- get-shader
  [src frag-vert]
  (let [shader (.createShader gl (case frag-vert :frag (.-FRAGMENT_SHADER gl) :vert (.-VERTEX_SHADER gl)))]
    (.shaderSource gl shader (str (case frag-vert :frag frag-header :vert vert-header) "\n" src))
    (.compileShader gl shader)
    (if (not (.getShaderParameter gl shader (.-COMPILE_STATUS gl)))
      (do
        (util/log "An error occured while compiling the shader: " (.getShaderInfoLog gl shader))
        nil)
      shader)))

(defn- load-shader-src
  "Loads a texture from vertex and fragment shader."
  ([frag-src vert-src]
  (let [frag (get-shader frag-src :frag)
        vert (get-shader vert-src :vert)
        prgrm (.createProgram gl)]
    (.attachShader gl prgrm vert)
    (.attachShader gl prgrm frag)
    (.linkProgram gl prgrm)
    (if (not (.getProgramParameter gl prgrm (.-LINK_STATUS gl)))
      (do
        (util/log "An error occured while linking the shader.")
        nil)
      prgrm)))
  ([frag-src]
   (load-shader-src frag-src default-vert-src)))

(defn- free-shader
  "Frees a shader from memory."
  [program]
  (.deleteShader gl program))

(defn- load-shader
  "Loads a shader."
  [load-type frag vert]
  (case load-type
    :string [(atom nil) (atom frag) (atom vert)]
    :file (let [atm-v (atom nil)
                atm-f (atom nil)
                request-v (js/XMLHttpRequest.)
                request-f (js/XMLHttpRequest.)]
            (.open request-v "GET" vert true)
            (.open request-f "GET" frag true)
            (set! (.-onload request-v) (fn [] (reset! atm-v (.-response request-v))))
            (set! (.-onload request-f) (fn [] (reset! atm-f (.-response request-f))))
            (.send request-v)
            (.send request-f)
            [(atom nil) atm-f atm-v])))

(defn- wrap-shader
  "Wraps a raw WebGl shader with information, such as uniform and attribute locations."
  [shader]
  {:program shader
   :projection-matrix-location (.getUniformLocation gl shader "projectionMatrix")
   :model-view-matrix-location (.getUniformLocation gl shader "modelViewMatrix")
   :position-location (.getAttribLocation gl shader "position")
   :diffuse-location (.getUniformLocation gl shader "tDiffuse")
   :normal-location (.getUniformLocation gl shader "tNormal")
   :color-location (.getUniformLocation gl shader "color")
   :uv-location (.getAttribLocation gl shader "aUv")})

(defn- shader-loaded?
  "Checks if the shader has loaded. If so, returns the actual program."
  [[atm-shdr atm-f atm-v]]
  (if-let [shdr @atm-shdr]
    shdr
    (when (and @atm-f @atm-v)
      (reset! atm-shdr (wrap-shader (load-shader-src @atm-f @atm-v))))))

(defn use-shader!
  "Bind a shader to gl context."
  [shader]
   (let [program (:program shader)]
     (when (not= *program* program)
       (def ^:dynamic *program* program)
       (.useProgram gl program)

       ;enable diffuse texture
       (.uniform1i gl (:diffuse-location shader), 0)

       ;enable normal texture
       (.uniform1i gl (:normal-location shader), 1)

       ;enable verticies
       (.enableVertexAttribArray gl (:position-location shader))

       ;enable
       )))

(defn shader-attr
  "Gets a shader attribute location"
  [shader attr]
  (.getAttribLocation gl shader attr))

(asset/add-asset-async :shader load-shader shader-loaded? free-shader)

;;;;; DRAW FUNCTIONS ;;;;;

;;;;; INIT ;;;;;

(defn init!
  "Initializes opengl. Returns gl context."
  [canvas]
  (def gl
    (if-let [glc (or (.getContext canvas "webgl") (.getContext canvas "experimental-webgl"))]
    (do
      (set! (.-viewportWidth glc) (.-width canvas))
      (set! (.-viewportHeight glc) (.-height canvas))
      (.enable glc (.-DEPTH_TEST glc))
      (.clearColor glc 0.0 0.0 0.0 1.0)
      (.enable glc (.-DEPTH_TEST glc))
      (.depthFunc glc (.-LEQUAL glc))
      glc)
    (do
      (util/log "Unable to load webgl context. Your browser may not support it.")
      nil)))
  (when gl
    (def default-shader (wrap-shader (load-shader-src default-frag-src default-vert-src)))
    (use-shader! default-shader))
  gl)
