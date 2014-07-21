(ns ezglib.render.gl
  (:require [ezglib.asset :as asset]
            [ezglib.util :as util]))

;;;;; CONSTANTS ;;;;;
; gl.BYTE is gl-byte
; gl.FLOAT is gl-float
; gl.INT is gl-int
; gl.KEEP is gl-keep
; gl.REPEAT is gl-repeat
; gl.REPLACE is gl-replace
; gl.SHORT is gl-short
; All other constants the same as the javascript versions except lowercase with dashes.
; For example, gl.ARRAY_BUFFER becomes array-buffer.

(def active-attributes 35721)
(def active-attribute-max-length 35722)
(def active-texture 34016)
(def active-uniforms 35718)
(def active-uniform-max-length 35719)
(def aliased-line-width-range 33902)
(def aliased-point-size-range 33901)
(def alpha 6406)
(def alpha-bits 3413)
(def always 519)
(def array-buffer 34962)
(def array-buffer-binding 34964)
(def attached-shaders 35717)
(def back 1029)
(def blend 3042)
(def blend-color 32773)
(def blend-dst-alpha 32970)
(def blend-dst-rgb 32968)
(def blend-equation 32777)
(def blend-equation-alpha 34877)
(def blend-equation-rgb 32777)
(def blend-src-alpha 32971)
(def blend-src-rgb 32969)
(def blue-bits 3412)
(def bool 35670)
(def bool-vec2 35671)
(def bool-vec3 35672)
(def bool-vec4 35673)
(def browser-default-webgl 37444)
(def buffer-size 34660)
(def buffer-usage 34661)
(def gl-byte 5120)
(def ccw 2305)
(def clamp-to-edge 33071)
(def color-attachment0 36064)
(def color-buffer-bit 16384)
(def color-clear-value 3106)
(def color-writemask 3107)
(def compile-status 35713)
(def compressed-texture-formats 34467)
(def constant-alpha 32771)
(def constant-color 32769)
(def context-lost-webgl 37442)
(def cull-face 2884)
(def cull-face-mode 2885)
(def current-program 35725)
(def current-vertex-attrib 34342)
(def cw 2304)
(def decr 7683)
(def decr-wrap 34056)
(def delete-status 35712)
(def depth-attachment 36096)
(def depth-bits 3414)
(def depth-buffer-bit 256)
(def depth-clear-value 2931)
(def depth-component 6402)
(def depth-component16 33189)
(def depth-func 2932)
(def depth-range 2928)
(def depth-stencil 34041)
(def depth-stencil-attachment 33306)
(def depth-test 2929)
(def depth-writemask 2930)
(def dither 3024)
(def dont-care 4352)
(def dst-alpha 772)
(def dst-color 774)
(def dynamic-draw 35048)
(def element-array-buffer 34963)
(def element-array-buffer-binding 34965)
(def equal 514)
(def fastest 4353)
(def gl-float 5126)
(def float-mat2 35674)
(def float-mat3 35675)
(def float-mat4 35676)
(def float-vec2 35664)
(def float-vec3 35665)
(def float-vec4 35666)
(def fragment-shader 35632)
(def framebuffer 36160)
(def framebuffer-attachment-object-name 36049)
(def framebuffer-attachment-object-type 36048)
(def framebuffer-attachment-texture-cube-map-face 36051)
(def framebuffer-attachment-texture-level 36050)
(def framebuffer-binding 36006)
(def framebuffer-complete 36053)
(def framebuffer-incomplete-attachment 36054)
(def framebuffer-incomplete-dimensions 36057)
(def framebuffer-incomplete-missing-attachment 36055)
(def framebuffer-unsupported 36061)
(def front 1028)
(def front-and-back 1032)
(def front-face 2886)
(def func-add 32774)
(def func-reverse-subtract 32779)
(def func-subtract 32778)
(def generate-mipmap-hint 33170)
(def gequal 518)
(def greater 516)
(def green-bits 3411)
(def high-float 36338)
(def high-int 36341)
(def incr 7682)
(def incr-wrap 34055)
(def info-log-length 35716)
(def gl-int 5124)
(def int-vec2 35667)
(def int-vec3 35668)
(def int-vec4 35669)
(def invalid-enum 1280)
(def invalid-framebuffer-operation 1286)
(def invalid-operation 1282)
(def invalid-value 1281)
(def invert 5386)
(def gl-keep 7680)
(def lequal 515)
(def less 513)
(def linear 9729)
(def linear-mipmap-linear 9987)
(def linear-mipmap-nearest 9985)
(def lines 1)
(def line-loop 2)
(def line-strip 3)
(def line-width 2849)
(def link-status 35714)
(def low-float 36336)
(def low-int 36339)
(def luminance 6409)
(def luminance-alpha 6410)
(def max-combined-texture-image-units 35661)
(def max-cube-map-texture-size 34076)
(def max-fragment-uniform-vectors 36349)
(def max-renderbuffer-size 34024)
(def max-texture-image-units 34930)
(def max-texture-size 3379)
(def max-varying-vectors 36348)
(def max-vertex-attribs 34921)
(def max-vertex-texture-image-units 35660)
(def max-vertex-uniform-vectors 36347)
(def max-viewport-dims 3386)
(def medium-float 36337)
(def medium-int 36340)
(def mirrored-repeat 33648)
(def nearest 9728)
(def nearest-mipmap-linear 9986)
(def nearest-mipmap-nearest 9984)
(def never 512)
(def nicest 4354)
(def none 0)
(def notequal 517)
(def no-error 0)
(def num-compressed-texture-formats 34466)
(def one 1)
(def one-minus-constant-alpha 32772)
(def one-minus-constant-color 32770)
(def one-minus-dst-alpha 773)
(def one-minus-dst-color 775)
(def one-minus-src-alpha 771)
(def one-minus-src-color 769)
(def out-of-memory 1285)
(def pack-alignment 3333)
(def points 0)
(def polygon-offset-factor 32824)
(def polygon-offset-fill 32823)
(def polygon-offset-units 10752)
(def red-bits 3410)
(def renderbuffer 36161)
(def renderbuffer-alpha-size 36179)
(def renderbuffer-binding 36007)
(def renderbuffer-blue-size 36178)
(def renderbuffer-depth-size 36180)
(def renderbuffer-green-size 36177)
(def renderbuffer-height 36163)
(def renderbuffer-internal-format 36164)
(def renderbuffer-red-size 36176)
(def renderbuffer-stencil-size 36181)
(def renderbuffer-width 36162)
(def renderer 7937)
(def gl-repeat 10497)
(def gl-replace 7681)
(def rgb 6407)
(def rgb5-a1 32855)
(def rgb565 36194)
(def rgba 6408)
(def rgba4 32854)
(def sampler-2d 35678)
(def sampler-cube 35680)
(def samples 32937)
(def sample-alpha-to-coverage 32926)
(def sample-buffers 32936)
(def sample-coverage 32928)
(def sample-coverage-invert 32939)
(def sample-coverage-value 32938)
(def scissor-box 3088)
(def scissor-test 3089)
(def shader-compiler 36346)
(def shader-source-length 35720)
(def shader-type 35663)
(def shading-language-version 35724)
(def gl-short 5122)
(def src-alpha 770)
(def src-alpha-saturate 776)
(def src-color 768)
(def static-draw 35044)
(def stencil-attachment 36128)
(def stencil-back-fail 34817)
(def stencil-back-func 34816)
(def stencil-back-pass-depth-fail 34818)
(def stencil-back-pass-depth-pass 34819)
(def stencil-back-ref 36003)
(def stencil-back-value-mask 36004)
(def stencil-back-writemask 36005)
(def stencil-bits 3415)
(def stencil-buffer-bit 1024)
(def stencil-clear-value 2961)
(def stencil-fail 2964)
(def stencil-func 2962)
(def stencil-index 6401)
(def stencil-index8 36168)
(def stencil-pass-depth-fail 2965)
(def stencil-pass-depth-pass 2966)
(def stencil-ref 2967)
(def stencil-test 2960)
(def stencil-value-mask 2963)
(def stencil-writemask 2968)
(def stream-draw 35040)
(def subpixel-bits 3408)
(def texture 5890)
(def texture0 33984)
(def texture1 33985)
(def texture2 33986)
(def texture3 33987)
(def texture4 33988)
(def texture5 33989)
(def texture6 33990)
(def texture7 33991)
(def texture8 33992)
(def texture9 33993)
(def texture10 33994)
(def texture11 33995)
(def texture12 33996)
(def texture13 33997)
(def texture14 33998)
(def texture15 33999)
(def texture16 34000)
(def texture17 34001)
(def texture18 34002)
(def texture19 34003)
(def texture20 34004)
(def texture21 34005)
(def texture22 34006)
(def texture23 34007)
(def texture24 34008)
(def texture25 34009)
(def texture26 34010)
(def texture27 34011)
(def texture28 34012)
(def texture29 34013)
(def texture30 34014)
(def texture31 34015)
(def texture-2d 3553)
(def texture-binding-2d 32873)
(def texture-binding-cube-map 34068)
(def texture-cube-map 34067)
(def texture-cube-map-negative-x 34070)
(def texture-cube-map-negative-y 34072)
(def texture-cube-map-negative-z 34074)
(def texture-cube-map-positive-x 34069)
(def texture-cube-map-positive-y 34071)
(def texture-cube-map-positive-z 34073)
(def texture-mag-filter 10240)
(def texture-min-filter 10241)
(def texture-wrap-s 10242)
(def texture-wrap-t 10243)
(def triangles 4)
(def triangle-fan 6)
(def triangle-strip 5)
(def unpack-alignment 3317)
(def unpack-colorspace-conversion-webgl 37443)
(def unpack-flip-y-webgl 37440)
(def unpack-premultiply-alpha-webgl 37441)
(def unsigned-byte 5121)
(def unsigned-int 5125)
(def unsigned-short 5123)
(def unsigned-short-4-4-4-4 32819)
(def unsigned-short-5-5-5-1 32820)
(def unsigned-short-5-6-5 33635)
(def validate-status 35715)
(def vendor 7936)
(def version 7938)
(def vertex-attrib-array-buffer-binding 34975)
(def vertex-attrib-array-enabled 34338)
(def vertex-attrib-array-normalized 34922)
(def vertex-attrib-array-pointer 34373)
(def vertex-attrib-array-size 34339)
(def vertex-attrib-array-stride 34340)
(def vertex-attrib-array-type 34341)
(def vertex-shader 35633)
(def viewport 2978)
(def zero 0)

;;;;; TYPED ARRAYS ;;;;;

(defn int8
  "Creates a native Int8Array for a given `collection`."
  [collection]
  (js/Int8Array. (clj->js collection)))

(defn uint8
  "Creates a native Uint8Array for a given `collection`."
  [collection]
  (js/Uint8Array. (clj->js collection)))

(defn uint8-clamped
  "Creates a native Uint8ClampedArray for a given `collection`."
  [collection]
  (js/Uint8ClampedArray. (clj->js collection)))

(defn int16
  "Creates a native Int16Array for a given `collection`."
  [collection]
  (js/Int16Array. (clj->js collection)))

(defn uint16
  "Creates a native Uint16Array for a given `collection`."
  [collection]
  (js/Uint16Array. (clj->js collection)))

(defn int32
  "Creates a native Int32Array for a given `collection`."
  [collection]
  (js/Int32Array. (clj->js collection)))

(defn uint32
  "Creates a native Uint32Array for a given `collection`."
  [collection]
  (js/Uint32Array. (clj->js collection)))

(defn float32
  "Creates a native Float32Array for a given `collection`."
  [collection]
  (js/Float32Array. (clj->js collection)))

(defn float64
  "Creates a native Float64Array for a given `collection`."
  [collection]
  (js/Float64Array. (clj->js collection)))

(defn typed-array?
  "Tests whether a given `value` is a typed array."
  [value]
  (let [value-type (type value)]
    (or
     (= value-type js/Int8Array)
     (= value-type js/Uint8Array)
     (= value-type js/Uint8ClampedArray)
     (= value-type js/Int16Array)
     (= value-type js/Uint16Array)
     (= value-type js/Int32Array)
     (= value-type js/Uint32Array)
     (= value-type js/Float32Array)
     (= value-type js/Float64Array))))

;;;;; CONTEXT ;;;;;

(defn create-context
  "Creates the opengl context."
  [canvas]
  (if-let [glc (or (.getContext canvas "webgl") (.getContext canvas "experimental-webgl"))]
    (do
      (set! (.-viewportWidth glc)  (.-width canvas))
      (set! (.-viewportHeight glc) (.-height canvas))
      (.enable glc depth-test)
      (.clearColor glc 0.0 0.0 0.0 1.0)
      (.enable glc depth-test)
      (.depthFunc glc lequal)
      glc)
    (do
      (util/log "Unable to load webgl context. Your browser may not support it.")
      nil)))

(defn context-width
  "Gets the width of the gl context's drawing buffer."
  [gl]
  (.-drawingBufferWidth gl))

(defn context-height
  "Gets the height of the gl context's drawing buffer."
  [gl]
  (.-drawingBufferHeight gl))

(defn context-lost?
  "Returns whether the context was lost."
  [gl]
  (.isContextLost gl))

(defn get-canvas
  "Returns the canvas used by the gl context."
  [gl]
  (.-canvas gl))

(defn get-viewport
  "Returns the current viewport for a given `gl-context` as a map with the form:

  {:x,
   :y,
   :width,
   :height}"
  [gl]
  (let [[x y w h] (.apply js/Array [] (.getParameter gl viewport))]
    {:x      x,
     :y      y,
     :width  w,
     :height h}))

(defn enabled?
  "Checks if a certain capability is enabled."
  [gl capability]
  (.isEnabled gl capability))

;;;;; BUFFER ;;;;;

(defn buffer
  "Creates a gl buffer with initialized data.

  :data must be a typed array-buffer.

  :target (optional) must be either ezglib.render.gl/array-buffer (default) or ezglib.render.gl/element-array-buffer.

  :usage (optional) must be either ezglib.render.gl/static-draw (default) or ezglib.render.gl/dynamic-draw.

  :item-size (optional)."
  [& {:keys [gl data target usage item-size]}]
  (let [buffer (.createBuffer gl)
        target (or target array-buffer)
        usage (or usage static-draw)]
    (.bindBuffer gl target buffer)
    (.bufferData gl target data usage)
    (when item-size
      (set! (.-itemSize buffer) item-size)
      (set! (.-numItems buffer) (quot (.-length data) item-size)))
    buffer))

;;;;; TEXTURE ;;;;;

(defn- handle-tex-loaded
  [gl image tex]
  (.bindTexture gl texture-2d tex)
  (.texImage2D gl texture-2d 0 rgba rgba unsigned-byte image)
  (.texParameteri gl texture-2d texture-mag-filter linear)
  (.texParameteri gl texture-2d texture-min-filter linear-mipmap-nearest)
  (.generateMipmap gl texture-2d)
  (.bindTexture gl texture-2d nil))

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

;;;;; SHADER ;;;;;

(def frag-header
  "
  precision mediump float;\n
  varying vec2 vUv;\n
  uniform sampler2D tDiffuse;\n
  uniform sampler2D tNormal;\n
  uniform vec4 color;\n
  uniform float time;\n
  ")

(def color-frag-src
  "void main(void) {\n
    gl_FragColor = color;\n
  }"
  )

(def texture-frag-src
  "void main(void) {\n
    gl_FragColor = color * texture2D(tDiffuse, vUv);\n
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
  uniform float time;\n
  ")

(def default-vert-src
  "void main(void) {\n
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);\n
    vUv = aUv;\n
  }"
  )

(defn- get-shader
  [gl src frag-vert]
  (let [shader (.createShader gl (case frag-vert :frag fragment-shader :vert vertex-shader))]
    (.shaderSource gl shader (str (case frag-vert :frag frag-header :vert vert-header) "\n" src))
    (.compileShader gl shader)
    (if (not (.getShaderParameter gl shader compile-status))
      (do
        (util/log "An error occured while compiling the shader: " (.getShaderInfoLog gl shader))
        nil)
      shader)))

(defn- load-shader-src
  "Loads a shader program from vertex and fragment shader."
  ([gl frag-src vert-src]
  (let [frag (get-shader gl frag-src :frag)
        vert (get-shader gl vert-src :vert)
        prgrm (.createProgram gl)]
    (.attachShader gl prgrm vert)
    (.attachShader gl prgrm frag)
    (.linkProgram gl prgrm)
    (if (not (.getProgramParameter gl prgrm link-status))
      (do
        (util/log "An error occured while linking the shader.")
        nil)
      prgrm)))
  ([gl frag-src]
   (load-shader-src gl frag-src default-vert-src)))

(defn- free-shader
  "Frees a shader from memory."
  [game program]
  (.deleteShader (:gl game) program))

(defn- load-shader
  "Loads a shader."
  [game load-type frag vert]
  (case load-type
    :string [(atom nil) (atom frag) (atom vert)]
    :file (let [atm-v (atom nil)
                atm-f (atom nil)
                request-v (js/XMLHttpRequest.)
                request-f (js/XMLHttpRequest.)]
            (.open request-v "GET" vert true)
            (.open request-f "GET" frag true)
            (set! (.-onload request-v) (fn [] (reset! atm-v (aget request-v "response"))))
            (set! (.-onload request-f) (fn [] (reset! atm-f (aget request-f "response"))))
            (.send request-v)
            (.send request-f)
            [(atom nil) atm-f atm-v])))

(defn- wrap-shader
  "Wraps a raw WebGl shader with information, such as uniform and attribute locations."
  [gl shader]
  {:program shader
   :gl gl
   :projection-matrix (.getUniformLocation gl shader "projectionMatrix")
   :model-view-matrix (.getUniformLocation gl shader "modelViewMatrix")
   :position (.getAttribLocation gl shader "position")
   :diffuse (.getUniformLocation gl shader "tDiffuse")
   :normal (.getUniformLocation gl shader "tNormal")
   :color (.getUniformLocation gl shader "color")
   :time (.getUniformLocation gl shader "time")
   :uv (.getAttribLocation gl shader "aUv")})

(defn make-shader
  "Creates a ezglib shader from fragment and vertex shader source."
  [gl frag vert]
  (wrap-shader gl (load-shader-src gl frag vert)))

(defn color-shader
  "Creates an ezglib color shader for a gl context."
  [gl]
  (make-shader gl color-frag-src default-vert-src))

(defn texture-shader
  "Creates an ezglib texture shader for a gl context."
  [gl]
  (make-shader gl texture-frag-src default-vert-src))

(defn- shader-loaded?
  "Checks if the shader has loaded. If so, returns the actual program."
  [game [atm-shdr atm-f atm-v]]
  (if-let [shdr @atm-shdr]
    shdr
    (when (and @atm-f @atm-v)
      (reset! atm-shdr (wrap-shader (:gl game) (load-shader-src (:gl game) @atm-f @atm-v))))))

(asset/add-asset
 :asset :shader
 :load-fn load-shader
 :is-done? shader-loaded?
 :free-fn free-shader)

(defn attrib-location
  "Gets the attribute location in an ezglib shader."
  [gl shader attrib-name]
  (.getAttribLocation gl (:program shader) attrib-name))

(defn uniform-location
  "Gets the uniform location in an ezglib shader."
  [gl shader uniform-name]
  (.getUniformLocation gl (:program shader) uniform-name))

(defn set-uniform!
  "Sets a uniform on an ezglib shader. Shader must be in use.

  type should be on of:
  :float, :vec2, :vec3, :vec4, :int, :ivec2, :ivec3, :ivec4, :mat2, :mat3, :mat4.

  values should be a typed array.

  location can be either the name, the numerical location, or the keyword of the uniform."
  [gl shader location type values]
  (let [program (:program shader)
        loc (if (string? location)
              (.getUniformLocation gl program location)
              (if (keyword? location)
                (location shader)
                location))]
    (case type
      :float  (.uniform1fv gl loc values)
      :vec2   (.uniform2fv gl loc values)
      :vec3   (.uniform3fv gl loc values)
      :vec4   (.uniform4fv gl loc values)
      :int    (.uniform1iv gl loc values)
      :ivec2  (.uniform2iv gl loc values)
      :ivec3  (.uniform3iv gl loc values)
      :ivec4  (.uniform4iv gl loc values)
      :mat2   (.uniformMatrix2fv gl loc false values)
      :mat3   (.uniformMatrix3fv gl loc false values)
      :mat4   (.uniformMatrix4fv gl loc false values)
      nil)
    gl))

(defn set-attribute!
  "Sets an attribute on an ezglib shader. Shader must be in use."
  [gl shader location buffer & {:keys [type normalized? stride offset components-per-vertex]}]
  (let [program (:program shader)
        loc (if (string? location)
              (.getUniformLocation gl program location)
              (if (keyword? location)
                (location shader)
                location))]
    (.bindBuffer gl array-buffer buffer)
    (.enableVertexAttribArray gl loc)
    (.vertexAttribPointer
     gl
     loc
     (or components-per-vertex (.-itemSize buffer) 3)
     (or type gl-float)
     (or normalized? false)
     (or stride 0)
     (or offset 0))
    gl))

(defn set-texture!
  "Sets the texture used by an ezglib shader.  Shader must be in use."
  ([gl shader texture location]
   (set-texture! gl shader texture location 0))
  ([gl shader texture location tex-unit]
   (let [unit (+ tex-unit texture0)
         program (:program shader)
         loc (if (string? location)
               (.getUniformLocation gl program location)
               (if (keyword? location)
                 (location shader)
                 location))]
     (.activeTexture gl unit)
     (.bindTexture gl texture-2d texture)
     (.uniform1i gl loc tex-unit)
     gl)))

(defn use-shader!
  "Sets the context to use an ezglib shader."
  [gl shader]
  (.useProgram gl (:program shader)))

;;;;; DRAW ;;;;;

(defn clear
  "Clears the gl context."
  [gl]
  (.clear gl (bit-or color-buffer-bit depth-buffer-bit stencil-buffer-bit))
  gl)

(defn clear-depth
  "Clears the depth buffer of the gl context."
  ([gl depth]
   (.clearDepth gl depth)
   (.clear gl depth-buffer-bit)
   gl)
  ([gl]
   (clear-depth gl 0)))

(defn clear-color
  "Clears the color buffer of the gl context."
  ([gl r g b a]
   (.clearColor gl r g b a)
   (.clear gl color-buffer-bit)
   gl)
  ([gl]
   (clear-color gl 0.0 0.0 0.0 1.0)))

(defn clear-stencil
  "Clears the stencil buffer of the gl context."
  ([gl index]
   (.clearStencil gl index)
   (.clear gl stencil-buffer-bit)
   gl)
  ([gl]
   (clear-stencil gl 0)))
