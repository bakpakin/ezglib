(ns ezglib.gl
  (:refer-clojure :exclude [byte float int keep repeat replace short])
  (:require [ezglib.util :as util]
            [ezglib.protocol :as p]))

;;;;; CONSTANTS

; All WebGL constants are the same as the javascript versions
; except lowercase and with dashes, clojure style.
; For example, gl.ARRAY_BUFFER becomes array-buffer.

(def ^:no-doc active-attributes 35721)
(def ^:no-doc active-attribute-max-length 35722)
(def ^:no-doc active-texture 34016)
(def ^:no-doc active-uniforms 35718)
(def ^:no-doc active-uniform-max-length 35719)
(def ^:no-doc aliased-line-width-range 33902)
(def ^:no-doc aliased-point-size-range 33901)
(def ^:no-doc alpha 6406)
(def ^:no-doc alpha-bits 3413)
(def ^:no-doc always 519)
(def ^:no-doc array-buffer 34962)
(def ^:no-doc array-buffer-binding 34964)
(def ^:no-doc attached-shaders 35717)
(def ^:no-doc back 1029)
(def ^:no-doc blend 3042)
(def ^:no-doc blend-color 32773)
(def ^:no-doc blend-dst-alpha 32970)
(def ^:no-doc blend-dst-rgb 32968)
(def ^:no-doc blend-equation 32777)
(def ^:no-doc blend-equation-alpha 34877)
(def ^:no-doc blend-equation-rgb 32777)
(def ^:no-doc blend-src-alpha 32971)
(def ^:no-doc blend-src-rgb 32969)
(def ^:no-doc blue-bits 3412)
(def ^:no-doc bool 35670)
(def ^:no-doc bool-vec2 35671)
(def ^:no-doc bool-vec3 35672)
(def ^:no-doc bool-vec4 35673)
(def ^:no-doc browser-default-webgl 37444)
(def ^:no-doc buffer-size 34660)
(def ^:no-doc buffer-usage 34661)
(def ^:no-doc byte 5120)
(def ^:no-doc ccw 2305)
(def ^:no-doc clamp-to-edge 33071)
(def ^:no-doc color-attachment0 36064)
(def ^:no-doc color-buffer-bit 16384)
(def ^:no-doc color-clear-value 3106)
(def ^:no-doc color-writemask 3107)
(def ^:no-doc compile-status 35713)
(def ^:no-doc compressed-texture-formats 34467)
(def ^:no-doc constant-alpha 32771)
(def ^:no-doc constant-color 32769)
(def ^:no-doc context-lost-webgl 37442)
(def ^:no-doc cull-face 2884)
(def ^:no-doc cull-face-mode 2885)
(def ^:no-doc current-program 35725)
(def ^:no-doc current-vertex-attrib 34342)
(def ^:no-doc cw 2304)
(def ^:no-doc decr 7683)
(def ^:no-doc decr-wrap 34056)
(def ^:no-doc delete-status 35712)
(def ^:no-doc depth-attachment 36096)
(def ^:no-doc depth-bits 3414)
(def ^:no-doc depth-buffer-bit 256)
(def ^:no-doc depth-clear-value 2931)
(def ^:no-doc depth-component 6402)
(def ^:no-doc depth-component16 33189)
(def ^:no-doc depth-func 2932)
(def ^:no-doc depth-range 2928)
(def ^:no-doc depth-stencil 34041)
(def ^:no-doc depth-stencil-attachment 33306)
(def ^:no-doc depth-test 2929)
(def ^:no-doc depth-writemask 2930)
(def ^:no-doc dither 3024)
(def ^:no-doc dont-care 4352)
(def ^:no-doc dst-alpha 772)
(def ^:no-doc dst-color 774)
(def ^:no-doc dynamic-draw 35048)
(def ^:no-doc element-array-buffer 34963)
(def ^:no-doc element-array-buffer-binding 34965)
(def ^:no-doc equal 514)
(def ^:no-doc fastest 4353)
(def ^:no-doc float 5126)
(def ^:no-doc float-mat2 35674)
(def ^:no-doc float-mat3 35675)
(def ^:no-doc float-mat4 35676)
(def ^:no-doc float-vec2 35664)
(def ^:no-doc float-vec3 35665)
(def ^:no-doc float-vec4 35666)
(def ^:no-doc fragment-shader 35632)
(def ^:no-doc framebuffer 36160)
(def ^:no-doc framebuffer-attachment-object-name 36049)
(def ^:no-doc framebuffer-attachment-object-type 36048)
(def ^:no-doc framebuffer-attachment-texture-cube-map-face 36051)
(def ^:no-doc framebuffer-attachment-texture-level 36050)
(def ^:no-doc framebuffer-binding 36006)
(def ^:no-doc framebuffer-complete 36053)
(def ^:no-doc framebuffer-incomplete-attachment 36054)
(def ^:no-doc framebuffer-incomplete-dimensions 36057)
(def ^:no-doc framebuffer-incomplete-missing-attachment 36055)
(def ^:no-doc framebuffer-unsupported 36061)
(def ^:no-doc front 1028)
(def ^:no-doc front-and-back 1032)
(def ^:no-doc front-face 2886)
(def ^:no-doc func-add 32774)
(def ^:no-doc func-reverse-subtract 32779)
(def ^:no-doc func-subtract 32778)
(def ^:no-doc generate-mipmap-hint 33170)
(def ^:no-doc gequal 518)
(def ^:no-doc greater 516)
(def ^:no-doc green-bits 3411)
(def ^:no-doc high-float 36338)
(def ^:no-doc high-int 36341)
(def ^:no-doc incr 7682)
(def ^:no-doc incr-wrap 34055)
(def ^:no-doc info-log-length 35716)
(def ^:no-doc int 5124)
(def ^:no-doc int-vec2 35667)
(def ^:no-doc int-vec3 35668)
(def ^:no-doc int-vec4 35669)
(def ^:no-doc invalid-enum 1280)
(def ^:no-doc invalid-framebuffer-operation 1286)
(def ^:no-doc invalid-operation 1282)
(def ^:no-doc invalid-value 1281)
(def ^:no-doc invert 5386)
(def ^:no-doc keep 7680)
(def ^:no-doc lequal 515)
(def ^:no-doc less 513)
(def ^:no-doc linear 9729)
(def ^:no-doc linear-mipmap-linear 9987)
(def ^:no-doc linear-mipmap-nearest 9985)
(def ^:no-doc lines 1)
(def ^:no-doc line-loop 2)
(def ^:no-doc line-strip 3)
(def ^:no-doc line-width 2849)
(def ^:no-doc link-status 35714)
(def ^:no-doc low-float 36336)
(def ^:no-doc low-int 36339)
(def ^:no-doc luminance 6409)
(def ^:no-doc luminance-alpha 6410)
(def ^:no-doc max-combined-texture-image-units 35661)
(def ^:no-doc max-cube-map-texture-size 34076)
(def ^:no-doc max-fragment-uniform-vectors 36349)
(def ^:no-doc max-renderbuffer-size 34024)
(def ^:no-doc max-texture-image-units 34930)
(def ^:no-doc max-texture-size 3379)
(def ^:no-doc max-varying-vectors 36348)
(def ^:no-doc max-vertex-attribs 34921)
(def ^:no-doc max-vertex-texture-image-units 35660)
(def ^:no-doc max-vertex-uniform-vectors 36347)
(def ^:no-doc max-viewport-dims 3386)
(def ^:no-doc medium-float 36337)
(def ^:no-doc medium-int 36340)
(def ^:no-doc mirrored-repeat 33648)
(def ^:no-doc nearest 9728)
(def ^:no-doc nearest-mipmap-linear 9986)
(def ^:no-doc nearest-mipmap-nearest 9984)
(def ^:no-doc never 512)
(def ^:no-doc nicest 4354)
(def ^:no-doc none 0)
(def ^:no-doc notequal 517)
(def ^:no-doc no-error 0)
(def ^:no-doc num-compressed-texture-formats 34466)
(def ^:no-doc one 1)
(def ^:no-doc one-minus-constant-alpha 32772)
(def ^:no-doc one-minus-constant-color 32770)
(def ^:no-doc one-minus-dst-alpha 773)
(def ^:no-doc one-minus-dst-color 775)
(def ^:no-doc one-minus-src-alpha 771)
(def ^:no-doc one-minus-src-color 769)
(def ^:no-doc out-of-memory 1285)
(def ^:no-doc pack-alignment 3333)
(def ^:no-doc points 0)
(def ^:no-doc polygon-offset-factor 32824)
(def ^:no-doc polygon-offset-fill 32823)
(def ^:no-doc polygon-offset-units 10752)
(def ^:no-doc red-bits 3410)
(def ^:no-doc renderbuffer 36161)
(def ^:no-doc renderbuffer-alpha-size 36179)
(def ^:no-doc renderbuffer-binding 36007)
(def ^:no-doc renderbuffer-blue-size 36178)
(def ^:no-doc renderbuffer-depth-size 36180)
(def ^:no-doc renderbuffer-green-size 36177)
(def ^:no-doc renderbuffer-height 36163)
(def ^:no-doc renderbuffer-internal-format 36164)
(def ^:no-doc renderbuffer-red-size 36176)
(def ^:no-doc renderbuffer-stencil-size 36181)
(def ^:no-doc renderbuffer-width 36162)
(def ^:no-doc renderer 7937)
(def ^:no-doc repeat 10497)
(def ^:no-doc replace 7681)
(def ^:no-doc rgb 6407)
(def ^:no-doc rgb5-a1 32855)
(def ^:no-doc rgb565 36194)
(def ^:no-doc rgba 6408)
(def ^:no-doc rgba4 32854)
(def ^:no-doc sampler-2d 35678)
(def ^:no-doc sampler-cube 35680)
(def ^:no-doc samples 32937)
(def ^:no-doc sample-alpha-to-coverage 32926)
(def ^:no-doc sample-buffers 32936)
(def ^:no-doc sample-coverage 32928)
(def ^:no-doc sample-coverage-invert 32939)
(def ^:no-doc sample-coverage-value 32938)
(def ^:no-doc scissor-box 3088)
(def ^:no-doc scissor-test 3089)
(def ^:no-doc shader-compiler 36346)
(def ^:no-doc shader-source-length 35720)
(def ^:no-doc shader-type 35663)
(def ^:no-doc shading-language-version 35724)
(def ^:no-doc short 5122)
(def ^:no-doc src-alpha 770)
(def ^:no-doc src-alpha-saturate 776)
(def ^:no-doc src-color 768)
(def ^:no-doc static-draw 35044)
(def ^:no-doc stencil-attachment 36128)
(def ^:no-doc stencil-back-fail 34817)
(def ^:no-doc stencil-back-func 34816)
(def ^:no-doc stencil-back-pass-depth-fail 34818)
(def ^:no-doc stencil-back-pass-depth-pass 34819)
(def ^:no-doc stencil-back-ref 36003)
(def ^:no-doc stencil-back-value-mask 36004)
(def ^:no-doc stencil-back-writemask 36005)
(def ^:no-doc stencil-bits 3415)
(def ^:no-doc stencil-buffer-bit 1024)
(def ^:no-doc stencil-clear-value 2961)
(def ^:no-doc stencil-fail 2964)
(def ^:no-doc stencil-func 2962)
(def ^:no-doc stencil-index 6401)
(def ^:no-doc stencil-index8 36168)
(def ^:no-doc stencil-pass-depth-fail 2965)
(def ^:no-doc stencil-pass-depth-pass 2966)
(def ^:no-doc stencil-ref 2967)
(def ^:no-doc stencil-test 2960)
(def ^:no-doc stencil-value-mask 2963)
(def ^:no-doc stencil-writemask 2968)
(def ^:no-doc stream-draw 35040)
(def ^:no-doc subpixel-bits 3408)
(def ^:no-doc texture 5890)
(def ^:no-doc texture0 33984)
(def ^:no-doc texture1 33985)
(def ^:no-doc texture2 33986)
(def ^:no-doc texture3 33987)
(def ^:no-doc texture4 33988)
(def ^:no-doc texture5 33989)
(def ^:no-doc texture6 33990)
(def ^:no-doc texture7 33991)
(def ^:no-doc texture8 33992)
(def ^:no-doc texture9 33993)
(def ^:no-doc texture10 33994)
(def ^:no-doc texture11 33995)
(def ^:no-doc texture12 33996)
(def ^:no-doc texture13 33997)
(def ^:no-doc texture14 33998)
(def ^:no-doc texture15 33999)
(def ^:no-doc texture16 34000)
(def ^:no-doc texture17 34001)
(def ^:no-doc texture18 34002)
(def ^:no-doc texture19 34003)
(def ^:no-doc texture20 34004)
(def ^:no-doc texture21 34005)
(def ^:no-doc texture22 34006)
(def ^:no-doc texture23 34007)
(def ^:no-doc texture24 34008)
(def ^:no-doc texture25 34009)
(def ^:no-doc texture26 34010)
(def ^:no-doc texture27 34011)
(def ^:no-doc texture28 34012)
(def ^:no-doc texture29 34013)
(def ^:no-doc texture30 34014)
(def ^:no-doc texture31 34015)
(def ^:no-doc texture-2d 3553)
(def ^:no-doc texture-binding-2d 32873)
(def ^:no-doc texture-binding-cube-map 34068)
(def ^:no-doc texture-cube-map 34067)
(def ^:no-doc texture-cube-map-negative-x 34070)
(def ^:no-doc texture-cube-map-negative-y 34072)
(def ^:no-doc texture-cube-map-negative-z 34074)
(def ^:no-doc texture-cube-map-positive-x 34069)
(def ^:no-doc texture-cube-map-positive-y 34071)
(def ^:no-doc texture-cube-map-positive-z 34073)
(def ^:no-doc texture-mag-filter 10240)
(def ^:no-doc texture-min-filter 10241)
(def ^:no-doc texture-wrap-s 10242)
(def ^:no-doc texture-wrap-t 10243)
(def ^:no-doc triangles 4)
(def ^:no-doc triangle-fan 6)
(def ^:no-doc triangle-strip 5)
(def ^:no-doc unpack-alignment 3317)
(def ^:no-doc unpack-colorspace-conversion-webgl 37443)
(def ^:no-doc unpack-flip-y-webgl 37440)
(def ^:no-doc unpack-premultiply-alpha-webgl 37441)
(def ^:no-doc unsigned-byte 5121)
(def ^:no-doc unsigned-int 5125)
(def ^:no-doc unsigned-short 5123)
(def ^:no-doc unsigned-short-4-4-4-4 32819)
(def ^:no-doc unsigned-short-5-5-5-1 32820)
(def ^:no-doc unsigned-short-5-6-5 33635)
(def ^:no-doc validate-status 35715)
(def ^:no-doc vendor 7936)
(def ^:no-doc version 7938)
(def ^:no-doc vertex-attrib-array-buffer-binding 34975)
(def ^:no-doc vertex-attrib-array-enabled 34338)
(def ^:no-doc vertex-attrib-array-normalized 34922)
(def ^:no-doc vertex-attrib-array-pointer 34373)
(def ^:no-doc vertex-attrib-array-size 34339)
(def ^:no-doc vertex-attrib-array-stride 34340)
(def ^:no-doc vertex-attrib-array-type 34341)
(def ^:no-doc vertex-shader 35633)
(def ^:no-doc viewport 2978)
(def ^:no-doc zero 0)

;;;;; TYPED ARRAYS

(defn array-gl-type
  "Returns the equivalent WebGl constant contained in a typed array."
  [value]
  (let [value-type (type value)]
    (or
     (if (= value-type js/Int8Array) byte)
     (if (= value-type js/Uint8Array) unsigned-byte)
     (if (= value-type js/Uint8ClampedArray) unsigned-byte)
     (if (= value-type js/Int16Array) short)
     (if (= value-type js/Uint16Array) unsigned-short)
     (if (= value-type js/Int32Array) int)
     (if (= value-type js/Uint32Array) unsigned-int)
     (if (= value-type js/Float32Array) float)
     (if (= value-type js/Float64Array) nil))))

;;;;; CONTEXT

(defn create-context
  "Creates the opengl context."
  [canvas]
  (if-let [gl (or (.getContext canvas "webgl") (.getContext canvas "experimental-webgl"))]
    (do
      (.viewport gl 0 0 (.-drawingBufferWidth gl) (.-drawingBufferHeight gl))
      (.enable gl depth-test)
      (.clearColor gl 0.0 0.0 0.0 1.0)
      (.enable gl depth-test)
      (.enable gl blend)
      (.depthFunc gl lequal)
      (.blendFunc gl src-alpha one-minus-src-alpha)
      (set! (.-currentShader gl) nil)
      gl)
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

(defn native-aspect
  "Gets the native aspect ratio of the gl context, i.e. the aspect ratio of the canvas."
  [gl]
  (/ (.-drawingBufferWidth gl) (.-drawingBufferHeight gl)))

(defn aspect
  "Gets the current aspect ratio of the gl context."
  [gl]
  (let [ps (.getParameter gl viewport)]
    (/ (aget ps 2) (aget ps 3))))

(defn context-lost?
  "Returns whether the context was lost."
  [gl]
  (.isContextLost gl))

(defn get-canvas
  "Returns the canvas used by the gl context."
  [gl]
  (.-canvas gl))

(defn get-viewport
  "Returns the current viewport for a given gl context as a map with the form:

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

(defn set-viewport!
  "Sets the current viewport for a given gl context. Expects a map with the form:

  {:x,
   :y,
   :width,
   :height}"
  [gl {:keys [x y width height] :as viewport}]
  (.viewport gl x y width height)
  gl)

(defn reset-viewport!
  "Resets the viewport to be the same size as the canvas."
  [gl]
  (.viewport gl 0 0 (context-width gl) (context-height gl)))

(defn enabled?
  "Checks if a certain capability is enabled."
  [gl capability]
  (.isEnabled gl capability))

(defn set-capability!
  "Enables or disables a capability."
  [gl capability enabled?]
  (if enabled?
    (.enable gl capability)
    (.disable gl capability))
  gl)

(defn set-capabilities!
  "Enables or disables capabilities."
  [gl & capabilities]
  (let [caps (apply hash-map capabilities)]
    (doseq [[capability enabled?] capabilities]
      (set-capability! gl capability enabled?)))
  gl)

;;;;; BUFFER

(defn buffer
  "Creates a gl buffer with initialized data.

  :data must be a typed array-buffer.

  :target (optional) must be either ezglib.gl/array-buffer (default) or ezglib.gl/element-array-buffer.

  :usage (optional) must be either ezglib.gl/static-draw (default) or ezglib.gl/dynamic-draw.

  :item-size (optional)."
  [gl & {:keys [data target usage item-size] :as args}]
  (let [buffer (.createBuffer gl)
        target (or target array-buffer)
        usage (or usage static-draw)]
    (.bindBuffer gl target buffer)
    (.bufferData gl target data usage)
    (if item-size
      (do
        (set! (.-itemSize buffer) item-size)
        (set! (.-numItems buffer) (quot (.-length data) item-size)))
      (do
        (set! (.-itemSize buffer) nil)
        (set! (.-numItems buffer) nil)))
    (set! (.-dataType buffer) (array-gl-type data))
    buffer))

;;;;; TEXTURE

(def ^:no-doc ^:private image-canvas (.createElement js/document "canvas"))

(defn- ^:no-doc  power-of-two?
  [x]
  (= 0 (bit-and x (dec x))))

(defn- ^:no-doc next-power-of-two
  [x]
  (cond
   (> x 4096) 8192
   (> x 2048) 4096
   (> x 1024) 2048
   (> x 512) 1024
   (> x 256) 512
   (> x 128) 256
   (> x 64) 128
   (> x 32) 64
   (> x 16) 32
   (> x 8) 16
   (> x 4) 8
   (> x 2) 4
   (> x 1) 2
   :default 1))

(defn- ^:no-doc make-power-of-two
  [img]
  (if (and (power-of-two? (.-width img)) (power-of-two? (.-height img)))
    img
    (let [c (.getContext image-canvas "2d")
          ptw (next-power-of-two (.-width img))
          pth (next-power-of-two (.-height img))]
      (set! (.-width image-canvas) ptw)
      (set! (.-height image-canvas) pth)
      (.clearRect c 0 0 (.-width image-canvas) (.-height image-canvas))
      (.drawImage c img 0 0 (.-width img) (.-height img))
      image-canvas)))

(defn load-texture
  "Loads a texture."
  [gl image min-filter mag-filter mipmap?]
  (let [iw (.-width image)
        ih (.-height image)
        new-image (make-power-of-two image)
        tex (.createTexture gl)]
    (.bindTexture gl texture-2d tex)
    (.texImage2D gl texture-2d 0 rgba rgba unsigned-byte new-image)
    (.texParameteri gl texture-2d texture-mag-filter min-filter)
    (.texParameteri gl texture-2d texture-min-filter mag-filter)
    (when mipmap?
      (.generateMipmap gl texture-2d))
    (.bindTexture gl texture-2d nil)
    (set! (.-gl tex) gl)
    (set! (.-imageWidth tex) iw)
    (set! (.-imageHeight tex) ih)
    (set! (.-uvWidth tex) (/ iw (.-width new-image)))
    (set! (.-uvHeight tex) (/ ih (.-height new-image)))
    tex))

(defn free-texture
  "Frees a loaded texture"
  [gl tex]
  (.deleteTexture gl tex))

(defn texture-width
  "Returns the width, in texels, of the texture."
  [tex]
  (.-imageWidth tex))

(defn texture-height
  "Returns the height, in texels, of the texture."
  [tex]
  (.-imageHeight tex))

(defn texture-size
  "Returns the size of the texture, in texels, as [width height]."
  [tex]
  [(.-imageWidth tex) (.-imageHeight tex)])

(defn texture-uv-width
  "Returns the width of the texture as a number between 0 and 1."
  [tex]
  (.-uvWidth tex))

(defn texture-uv-height
  "Returns the width of the texture as a number between 0 and 1."
  [tex]
  (.-uvHeight tex))

(defn texture-uv-size
  "Returns the uv size of the texture as [width height]."
  [tex]
  [(.-uvWidth tex) (.-uvHeight tex)])

(defn bind-texture!
  "Binds the texture to the context to be used by an ezglib shader."
  ([gl texture]
   (bind-texture! gl texture 0))
  ([gl texture tex-unit]
   (let [unit (+ tex-unit texture0)]
     (.activeTexture gl unit)
     (.bindTexture gl texture-2d texture)
     gl)))

;;;;; SHADER

(defn- ^:no-doc make-shader
  [gl src frag-vert]
  (let [shader (.createShader gl (case frag-vert :frag fragment-shader :vert vertex-shader))]
    (.shaderSource gl shader src)
    (.compileShader gl shader)
    (if (not (.getShaderParameter gl shader compile-status))
      (do
        (util/log "An error occured while compiling the shader: " (.getShaderInfoLog gl shader))
        nil)
      shader)))

(defn- ^:no-doc make-program
  [gl frag-src vert-src]
  (let [frag (make-shader gl frag-src :frag)
        vert (make-shader gl vert-src :vert)
        prgrm (.createProgram gl)]
    (.attachShader gl prgrm vert)
    (.attachShader gl prgrm frag)
    (.linkProgram gl prgrm)
    (if (not (.getProgramParameter gl prgrm link-status))
      (do
        (util/log "An error occured while linking the shader.")
        nil)
      prgrm)))

(defn- ^:no-doc wrap-program
  [gl prgrm]
  (let [attributes (atom {})
        uniforms (atom {})
        attribute-types (atom {})
        uniform-types (atom {})
        active-uniforms (.getProgramParameter gl prgrm active-uniforms)
        active-attributes (.getProgramParameter gl prgrm active-attributes)]
    (doseq [i (range active-uniforms)]
      (let [u (.getActiveUniform gl prgrm i)
            n (.-name u)
            kn (keyword n)]
        (swap! uniforms assoc kn (.getUniformLocation gl prgrm n))
        (swap! uniform-types assoc kn (.-type u))))
    (doseq [i (range active-attributes)]
        (let [a (.getActiveAttrib gl prgrm i)
              n (.-name a)
              kn (keyword n)]
          (swap! attributes assoc kn (.getAttribLocation gl prgrm n))
          (swap! attribute-types assoc kn (.-type a))))
    (set! (.-gl prgrm) gl)
    (set! (.-uniforms prgrm) @uniforms)
    (set! (.-attributes prgrm) @attributes)
    (set! (.-uniformTypes prgrm) @uniform-types)
    (set! (.-attributeTypes prgrm) @attribute-types)
    prgrm))

(defn load-shader
  "Loads an ezglib shader from fragment and vertex shader source."
  [gl frag vert]
  (wrap-program gl (make-program gl frag vert)))

(defn free-shader
  "Frees a shader from memory."
  [gl program]
  (.deleteShader gl program))

(defn current-shader
  "Gets the ezglib shader currently bound to the gl context."
  [gl]
  (.-currentShader gl))

(defn set-uniform!
  "Sets a uniform on the current ezglib shader.

  value should be a typed array or a type that extends ezglib.protocol.ITypedArray.

  location must be the keyword form of the uniform name."
  [gl location value]
  (let [program (.-currentShader gl)
        loc (location (.-uniforms program))
        t (location (.-uniformTypes program))
        v (if (or (number? value) (util/typed-array? value))
            value
            (p/-typed-array value))]
    (if (number? v)
      (case t

        35670    (.uniform1i gl loc v) ;bool
        5126     (.uniform1f gl loc v) ;float
        5124     (.uniform1i gl loc v) ;int

        35678    (.uniform1i gl loc v) ;sampler-2d
        35680    (.uniform1i gl loc v) ;sampler-cube

        (util/log "Couldn't set uniform \"" (name location) "\" on shader with value: " value))

      (case t

        35670    (.uniform1iv gl loc v) ;bool
        35671    (.uniform2iv gl loc v) ;bool-vec2
        35672    (.uniform3iv gl loc v) ;bool-vec3
        35673    (.uniform4iv gl loc v) ;bool-vec4

        5126     (.uniform1fv gl loc v) ;float
        35664    (.uniform2fv gl loc v) ;float-vec2
        35665    (.uniform3fv gl loc v) ;float-vec3
        35666    (.uniform4fv gl loc v) ;float-vec4

        5124     (.uniform1iv gl loc v) ;int
        35667    (.uniform2iv gl loc v) ;int-vec2
        35668    (.uniform3iv gl loc v) ;int-vec3
        35669    (.uniform4iv gl loc v) ;int-vec4

        35674    (.uniformMatrix2fv gl loc false v) ;float-mat2
        35675    (.uniformMatrix3fv gl loc false v) ;float-mat3
        35676    (.uniformMatrix4fv gl loc false v) ;float-mat4

        35678    (.uniform1iv gl loc v) ;sampler-2d
        35680    (.uniform1iv gl loc v) ;sampler-cube

        (util/log "Couldn't set uniform \"" (name location) "\" on shader with value: " value)))
    gl))

(defn- ^:no-doc vertex-type
  [shader location]
  (case (location (.-attributeTypes shader))

    35670    unsigned-byte ;bool
    35671    unsigned-byte ;bool-vec2
    35672    unsigned-byte ;bool-vec3
    35673    unsigned-byte ;bool-vec4

    5126     float ;float
    35664    float ;float-vec2
    35665    float ;float-vec3
    35666    float ;float-vec4

    5124     short ;int
    35667    short ;int-vec2
    35668    short ;int-vec3
    35669    short ;int-vec4

    35674    float ;float-mat2
    35675    float ;float-mat3
    35676    float ;float-mat4

    float))

(defn set-attribute!
  "Sets an attribute on the current ezglib shader."
  [gl location {:keys [buffer normalized? stride type offset components-per-vertex] :as opts}]
  (let [program (.-currentShader gl)
        loc (location (.-attributes program))]
    (.bindBuffer gl array-buffer buffer)
    (.enableVertexAttribArray gl loc)
    (.vertexAttribPointer
     gl
     loc
     (or components-per-vertex (.-itemSize buffer) 3)
     (or type (vertex-type program location))
     (or normalized? false)
     (or stride 0)
     (or offset 0))
    gl))

(defn set-attribute*!
  "Quickly sets an attribute on the current ezglib shader with default parameters."
  [gl location buffer]
  (let [program (.-currentShader gl)
        loc (location (.-attributes program))]
    (.bindBuffer gl array-buffer buffer)
    (.enableVertexAttribArray gl loc)
    (.vertexAttribPointer
     gl
     loc
     (.-itemSize buffer)
     (vertex-type program location)
     false
     0
     0)
    gl))

(defn use-shader!
  "Sets the context to use an ezglib shader."
  [gl shader & {:keys [uniforms attributes textures] :as opts}]

  (when-let [cs (current-shader gl)]
    (doseq [[_ loc] (.-attributes cs)]
      (.disableVertexAttribArray gl loc)))

  (.useProgram gl shader)

  (set! (.-currentShader gl) shader)

  (when opts

    (when textures
      (doseq [[tex-unit tex] textures]
        (bind-texture! gl tex tex-unit)))

    (when uniforms
      (doseq [[loc values] uniforms]
        (set-uniform! gl loc values)))

    (when attributes
      (doseq [[loc opts] attributes]
        (set-attribute! gl loc opts)))))

;;;;; BLEND

(defn blend-func!
  "Sets the gl context to use the given blend function."
  [gl src dest]
  (.blendFunc gl src dest))

;;;;; DRAW

(defn clear!
  "Clears the gl context."
  [gl]
  (.clear gl (bit-or color-buffer-bit depth-buffer-bit stencil-buffer-bit))
  gl)

(defn clear-depth!
  "Clears the depth buffer of the gl context."
  ([gl depth]
   (.clearDepth gl depth)
   (.clear gl depth-buffer-bit)
   gl)
  ([gl]
   (clear-depth! gl 0)))

(defn clear-color!
  "Clears the color buffer of the gl context."
  ([gl r g b a]
   (.clearColor gl r g b a)
   (.clear gl color-buffer-bit)
   gl)
  ([gl]
   (clear-color! gl 0.0 0.0 0.0 1.0)))

(defn clear-stencil!
  "Clears the stencil buffer of the gl context."
  ([gl index]
   (.clearStencil gl index)
   (.clear gl stencil-buffer-bit)
   gl)
  ([gl]
   (clear-stencil! gl 0)))

(defn draw-arrays!
  "Draws arrays to gl context."
  ([gl draw-mode first count]
   (.drawArrays gl draw-mode first count)
   gl)
  ([gl draw-mode count]
   (.drawArrays gl draw-mode 0 count)
   gl))

(defn draw-elements!
  "Draws the elements of bound attribute arrays to gl context."
  ([gl buffer draw-mode count offset]
   (.bindBuffer gl element-array-buffer buffer)
   (.drawElements gl draw-mode count (.-dataType buffer) offset)
   gl))

(defn draw!
  "Draws to the gl context.

  Options:

  :shader - ezglib shader to draw with.

  :draw-mode - A draw-mode such as ezglib.gl/triangles

  :count - the number of verticies to draw.

  :first (optional) - the first index to begin drawing. Default is 0.

  :uniforms (optional) - a map of shader uniforms to their values. Uniform names
  are given in their keyword form (a uniform named \"uMyUniform\" becomes :uMyUniform),
  and values can be numbers, javascript typed arrays, or any type that implements
  ezglib.protocol.ITypedArray.

  :attributes (optional) - similar to :uniforms, but instead of typed arrays, values
  must be buffers created via ezglib.gl/buffer.

  :textures (optional) - a map of integers from 0 to 32 to textures. In order to
  use textures in shaders, set the texture uniform in :uniforms to the same number specified in :textures.

  :blend-src (optional) - The source component for the belnding function, if blending is enabled.

  :blend-dest (optional) - The destination component for the belnding function, if blending is enabled.

  :capabilities (optional) - a map of capabilities to booleans indicating if they are enabled.

  :element-buffer (optional) - When drawing by element, :element-buffer should be a buffer created via
  ezglib.gl/buffer of type UInteger16. By default, drawArrays is called.

  :element-offset (optional) - When drawing elements, :element-offset specifies the offset from which
  to begin drawing."

  [gl & {:keys [uniforms attributes textures shader draw-mode first count
                blend-src blend-dest capabilities element-buffer element-offset]}]

  (use-shader! gl shader :uniforms uniforms :attributes attributes :textures textures)

  (doseq [[capability enabled?] capabilities]
    (set-capability! gl capability enabled?))

  (when (and blend-src blend-dest)
    (blend-func! gl blend-src blend-dest))

  (if (nil? element-buffer)
    (.drawArrays gl draw-mode (or first 0) count)
    (do
      (.bindBuffer gl element-array-buffer element-buffer)
      (.drawElements gl draw-mode count (.-dataType element-buffer) (or element-offset 0))))

  gl)
