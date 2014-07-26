(ns ezglib.render
  (:require [ezglib.gl :as gl]
            [ezglib.util :as util]
            [ezglib.asset :as asset]
            [ezglib.math :as m]
            [ezglib.protocol :as p]))

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
  }")

(def texture-frag-src
  "void main(void) {\n
    gl_FragColor = color * texture2D(tDiffuse, vUv);\n
  }")

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
  }")

(defn- load-shader-string
  [game frag vert]
  [(atom nil) (atom frag) (atom vert)])

(defn- load-shader-file
  [game frag vert]
  (let [gl (:gl game)
        rf (js/XMLHttpRequest.)
        rv (js/XMLHttpRequest.)
        atm-f (atom nil)
        atm-v (atom nil)]
    (.open rf "GET" frag true)
    (.open rv "GET" vert true)
    (set! (.-onload rf) #(reset! atm-f (.-response rf)))
    (set! (.-onload rv) #(reset! atm-v (.-response rv)))
    (.send rf)
    (.send rv)
    [(atom nil) atm-f atm-v]))

(defn- load-shader
  ([game load-type frag vert]
   (case load-type
     :string (load-shader-string game frag vert)
     :file (load-shader-file game frag vert)))
  ([game frag vert]
   (load-shader-string game frag vert)))

(defn- shader-loaded?
  [game [shader-atm frag-atm vert-atm]]
  (if-let [shader @shader-atm]
    shader
    (if (and @frag-atm @vert-atm)
      (reset! shader-atm (gl/load-shader (:gl game) (str frag-header "\n" @frag-atm) (str vert-header "\n" @vert-atm))))))

(defn- free-shader
  [game shader]
  (gl/free-shader (:gl game) shader))

(asset/add-asset
 :asset :shader
 :load-fn load-shader
 :free-fn free-shader
 :is-done? shader-loaded?)

(defn texture-shader
  "Creates an ezglib texture shader for a gl context."
  [gl]
  (gl/load-shader gl (str frag-header texture-frag-src) (str vert-header default-vert-src)))

;;;;; TEXTURE ;;;;;

(defn- load-texture
  ([game url min-filter mag-filter mipmap?]
   (let [atm (atom nil)
         image (js/Image.)]
     (set! (.-src image) url)
     (set! (.-crossOrigin image) "anonymous")
     (set! (.-onload image) (fn []
                             (reset! atm (gl/load-texture (:gl game) image min-filter mag-filter mipmap?))))
     atm))
  ([game url min-filter mag-filter]
   (load-texture game url min-filter mag-filter false))
  ([game url]
   (load-texture game url gl/linear gl/linear false)))

(defn- free-texture
  [game texture]
  (gl/free-texture (:gl game) texture))

(defn- texture-loaded?
  [game tex-atm]
  (if-let [tex @tex-atm] tex))

(asset/add-asset
 :asset :texture
 :load-fn load-texture
 :free-fn free-texture
 :is-done? texture-loaded?)

;;;;; CAMERAS ;;;;;

(deftype Camera2D [x y hw hh angle pos matrix]
  p/I3D
  (-matrix [_] matrix)
  p/IPosition
  (-position [_] pos))

(defn camera-2d
  "Constructs a 2D camera."
  ([x y hw hh]
   (camera-2d x y hw hh 0))
  ([x y hw hh angle]
   (Camera2D.
    x
    y
    hw
    hh
    angle
    (m/Vec3. x y 0 nil)
    (m/mult
     (m/ortho (- x hw) (+ x hw) (- y hh) (+ y hh) -1000000 1000000)
     (m/rotate-z angle)))))

;;;;; SPRITE ;;;;;

(deftype Sprite [gl tex verts uv]
  p/IDrawable
  (-draw [_]
         (gl/bind-texture! gl tex)
         (gl/set-attribute*! gl :position verts)
         (gl/set-attribute*! gl :aUv uv)
         (gl/draw-arrays gl gl/triangle-strip 4)))

(defn sprite
  "Creates a new sprite."
  [tex]
  (let [w (.-imageWidth tex)
        h (.-imageHeight tex)
        u (.-uvWidth tex)
        v (.-uvHeight tex)]
    (Sprite.
     (.-gl tex)
     tex
     (gl/buffer (.-gl tex)
       :data (js/Float32Array. (array 0 0 0 h w 0 w h)))
     (gl/buffer (.-gl tex)
       :data (js/Float32Array. (array 0 0 0 v u 0 u v))))))

;;;;; TEXT ;;;;;

(def ^:private font-canvas (.createElement js/document "canvas"))
(def ^:private font-ctx (.getContext font-canvas "2d"))

(def ^:private text-div (.createElement js/document "div"))
(def ^:private div-style (.-style text-div))
(set! (.-position div-style) "absolute")
(set! (.-visibility div-style) "hidden")
(set! (.-width div-style) "auto")
(set! (.-height div-style) "auto")

(defn- string-size
  "Returns the dimensions of a string as [width height]."
  [font-name font-size string]
  (.appendChild (.-body js/document) text-div)
  (set! (.-font div-style) (str font-size "px " font-name))
  (set! (.-innerHTML text-div) string)
  (let [ret [(.-clientWidth text-div) (.-clientHeight text-div)]]
      (.removeChild (.-body js/document) text-div)
      ret))

(defn- load-text
  "Loads text for web gl rendering."
  [game font-name font-size text]
  (let [gl (:gl game)
        [w h] (string-size font-name font-size text)]
    (set! (.-width font-canvas) (+ 2 w))
    (set! (.-height font-canvas) (+ 2 h))
    (set! (.-font font-ctx) (str font-size "px " font-name))
    (set! (.-fillStyle font-ctx) "#FFFFFF")
    (set! (.-textBaseline font-ctx) "top")
    (.clearRect font-ctx 0 0 (+ 2 w) (+ 2 h))
    (.fillText font-ctx text 0 0)
    (sprite (gl/load-texture gl font-canvas gl/linear gl/linear false))))

(defn- free-text
  [game text]
  (gl/free-texture (:gl game) text))

(asset/add-asset
 :asset :text
 :load-fn load-text
 :free-fn free-text)

;;;;; GRAPH ;;;;;

(deftype GraphNode [^:mutable children ^:mutable transform ^:mutable local-transform])

(defn node
  "Creates a scene graph node."
  ([matrix & children]
   (GraphNode. children nil matrix))
  ([matrix]
   (GraphNode. (list) nil matrix))
  ([]
   (GraphNode. (list) nil (m/m-identity 4))))
