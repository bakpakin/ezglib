(ns ezglib.render.shader
  (:require [ezglib.asset :as asset]
            [ezglib.util :as util]
            [ezglib.render.gl :as gl]))

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
  [gl src frag-vert]
  (let [shader (.createShader gl (case frag-vert :frag gl/fragment-shader :vert gl/vertex-shader))]
    (.shaderSource gl shader (str (case frag-vert :frag frag-header :vert vert-header) "\n" src))
    (.compileShader gl shader)
    (if (not (.getShaderParameter gl shader gl/compile-status))
      (do
        (util/log "An error occured while compiling the shader: " (.getShaderInfoLog gl shader))
        nil)
      shader)))

(defn- load-shader-src
  "Loads a texture from vertex and fragment shader."
  ([gl frag-src vert-src]
  (let [frag (get-shader gl frag-src :frag)
        vert (get-shader gl vert-src :vert)
        prgrm (.createProgram gl)]
    (.attachShader gl prgrm vert)
    (.attachShader gl prgrm frag)
    (.linkProgram gl prgrm)
    (if (not (.getProgramParameter gl prgrm gl/link-status))
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
   :projection-matrix-location (.getUniformLocation gl shader "projectionMatrix")
   :model-view-matrix-location (.getUniformLocation gl shader "modelViewMatrix")
   :position-location (.getAttribLocation gl shader "position")
   :diffuse-location (.getUniformLocation gl shader "tDiffuse")
   :normal-location (.getUniformLocation gl shader "tNormal")
   :color-location (.getUniformLocation gl shader "color")
   :uv-location (.getAttribLocation gl shader "aUv")})

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
