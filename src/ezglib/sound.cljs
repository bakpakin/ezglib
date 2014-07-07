(ns ezglib.sound
  (:require [ezglib.asset :as asset]
            [ezglib.util :as util]))

(defn- create-context
  []
    (js/window.AudioContext.))

(def ^:private context (create-context))

(defn- load-sound
  "Loads a sound given a url."
  [url]
  (let [request (js/XMLHttpRequest.)
        out (atom nil)]
    (.open request "GET" url true)
    (set! (.-responseType request) "arraybuffer")
    (set!
     (.-onload request)
     (fn []
       ;Magical Hackery for Advanced compilation - really ugly
       (.decodeAudioData
        context
        (.-response request)
        (fn [buffer] (reset! out buffer))
        (fn [] (reset! out nil)))))
    (.send request)
    out))

(asset/add-asset-async :sound load-sound (fn [snd] @snd))

(defn play
  "Plays a sound."
  [sound]
  (let [source (.createBufferSource context)]
    (aset source "buffer" sound)
    (.connect source (.-destination context))
    (.start source 0)))
