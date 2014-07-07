(ns ezglib.gl
  (:require [ezglib.asset :as asset]
            [ezglib.util :as util])
  (:import [goog.math Matrix]))

;The WebGl context
(declare gl)

(defn clear!
  "Clears the gl context."
  []
  (.clear gl (bit-or (aget gl "COLOR_BUFFER_BIT") (aget gl "DEPTH_BUFFER_BIT"))))

;;;;; TEXTURE FUNCTIONS ;;;;;

(defn- handle-tex-loaded
  [image tex]
  (.bindTexture gl (aget gl "TEXTURE_2D") tex)
  (.texImage2D gl (aget gl "TEXTURE_2D") 0 (aget gl "RGBA") (aget gl "RGBA") (aget gl "UNSIGNED_BYTE") image)
  (.texParameteri gl (aget gl "TEXTURE_2D") (aget gl "TEXTURE_MAG_FILTER") (aget gl "LINEAR"))
  (.texParameteri gl (aget gl "TEXTURE_2D") (aget gl "TEXTURE_MIN_FILTER") (aget gl "LINEAR_MIPMAP_NEAREST"))
  (.generateMipmap gl (aget gl "TEXTURE_2D"))
  (.bindTexture gl (aget gl "TEXTURE_2D") nil))

(defn- is-tex-loaded?
  [[tex atm]]
  (when @atm tex))

(defn- load-texture
  "Loads a texture."
  [url]
  (let [tex (.createTexture gl)
        image (js/Image.)
        atm (atom false)]
    (aset image "src" url)
    (aset image "onload" (fn []
                             (handle-tex-loaded image tex)
                             (reset! atm true)))
    [tex atm]))

(defn- free-texture
  "Frees a loaded texture"
  [tex]
  (.deleteTexture gl tex))

(asset/add-asset-async :texture load-texture is-tex-loaded? free-texture)

;;;;; SHADER FUNCTIONS ;;;;;

(def ^:dynamic *shader* nil)
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
  (let [shader (.createShader gl (case frag-vert :frag (aget gl "FRAGMENT_SHADER") :vert (aget gl "VERTEX_SHADER")))]
    (.shaderSource gl shader (str (case frag-vert :frag frag-header :vert vert-header) "\n" src))
    (.compileShader gl shader)
    (if (not (.getShaderParameter gl shader (aget gl "COMPILE_STATUS")))
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
    (if (not (.getProgramParameter gl prgrm (aget gl "LINK_STATUS")))
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
            (aset request-v "onload" (fn [] (reset! atm-v (aget request-v "response"))))
            (aset request-f "onload" (fn [] (reset! atm-f (aget request-f "response"))))
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
       (def ^:dynamic *shader* shader)
       (.useProgram gl program)

       ;enable diffuse texture
       (.uniform1i gl (:diffuse-location shader) 0)

       ;enable normal texture
       (.uniform1i gl (:normal-location shader) 1)

       ;enable verticies
       (.enableVertexAttribArray gl (:position-location shader))
       )))

(defn shader-attr
  "Gets a shader attribute location"
  [shader attr]
  (.getAttribLocation gl shader attr))

(asset/add-asset-async :shader load-shader shader-loaded? free-shader)

;;;;; MATRIX FUNCTIONS ;;;;;

(defn apply-projection!
  "Sends the projection matrix to the gl context."
  [matrix]
  (.uniformMatrix4fv gl (:projection-matrix-location *shader*) false (js/Float32Array. (aget matrix "array_"))))

(defn apply-model-view!
  "Sends the model-view matrix to the gl context."
  [matrix]
  (.uniformMatrix4fv gl (:model-view-matrix-location *shader*) false (js/Float32Array. (aget matrix "array_"))))

(defn mat-perspective
  "Creates a perspective matrix."
  ([fov aspect near far left-handed?]
    (if (or (<= fov 0) (= aspect 0))
      nil
      (let [frustum-depth (- far near)
            one-over-depth (/ 1 frustum-depth)
            one-one (/ 1 (.tan js/Math (* .5 fov)))
            zero-zero (* (if left-handed? 1 -1) one-one (/ aspect))
            two-two (* far one-over-depth)
            three-two (* (- far) near one-over-depth)]
        (goog.math.Matrix. (array (array zero-zero 0 0 0)
                         (array 0 one-one 0 0)
                         (array 0 0 two-two 1)
                         (array 0 0 three-two 0))))))
  ([fov aspect near far]
   (mat-perspective fov aspect near far true)))

(defn mat-orthographic
  "Creates an orthographic matrix."
  [x1 x2 y1 y2 z1 z2]
  (let [dx (- x2 x1)
        dy (- y2 y1)
        dz (- z2 z1)]
    (goog.math.Matrix. (array (array (/ 2 dx) 0 0 (- (/ (+ x1 x2) dx)))
                     (array 0 (/ 2 dy) 0 (- (/ (+ y1 y2) dy)))
                     (array 0 0 (/ -2 dz) (- (/ (+ z1 z2) dz)))
                     (array 0 0 0 1)))))

(defn mat-identity
  "Creates an identity matrix."
  [size]
  ((aget goog.math.Matrix "createIdentityMatrix") size))

(defn mat-translation
  "Returns a 4x4 matrix representing a translation."
  ([x y z]
   (goog.math.Matrix. (array (array 1 0 0 (- x))
                             (array 0 1 0 (- y))
                             (array 0 0 1 (- z))
                             (array 0 0 0 1))))
  ([[x y z]]
   (mat-translation x y z)))

;;;;; BUFFER FUNCTIONS ;;;;;

(defn buffer
  "Creates a buffer and sends it to the gl context."
  [arr item-size]
  (if (not (vector? arr))
    (let [buffer (.createBuffer gl)]
      (.bindBuffer gl (aget gl "ARRAY_BUFFER") buffer)
      (.bufferData gl (aget gl "ARRAY_BUFFER") (js/Float32Array. arr) (aget gl "STATIC_DRAW"))
      (aset buffer "itemSize" item-size)
      buffer)
    (buffer (apply array arr) item-size)))

(defn draw-buffer
  "Draws the buffer to the gl context."
  ([vert-buffer]
    (.bindBuffer gl (aget gl "ARRAY_BUFFER") vert-buffer)
    (.vertexAttribPointer gl (:position-location *shader*) 3 (aget gl "FLOAT") false 0 0)
    (.drawArrays gl (aget gl "TRIANGLES") 0 (/ (aget buffer "length") (aget buffer "itemSize")))))

;;;;; INIT ;;;;;

(defn init!
  "Initializes opengl. Returns gl context."
  [canvas]
  (def gl
    (if-let [glc (or (.getContext canvas "webgl") (.getContext canvas "experimental-webgl"))]
    (do
      (aset glc "viewportWidth" (aget canvas "width"))
      (aset glc "viewportHeight" (aget canvas "height"))
      (.enable glc (aget glc "DEPTH_TEST"))
      (.clearColor glc 0.0 0.0 0.0 1.0)
      (.enable glc (aget glc "DEPTH_TEST"))
      (.depthFunc glc (aget glc "LEQUAL"))
      glc)
    (do
      (util/log "Unable to load webgl context. Your browser may not support it.")
      nil)))
  (when gl
    (def default-shader (wrap-shader (load-shader-src default-frag-src default-vert-src)))
    (use-shader! default-shader))
  gl)
