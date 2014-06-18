(ns ezglib.sound
  (:require [ezglib.asset :as asset]))

;The AudioContext
(declare context)

(defn init!
  "Initializes WebAudio. Returns the new context."
  []
  (try
    (do
      (set! (.-AudioContext js/window) (or (.-AudioContext js/window) (.-webkitAudioContext js/window)))
      (def ^:private context (js/AudioContext.))
      context)
    (catch js/Object e
      (.log js/console "Your browser does not support WebAudio."))))

(defn load-sound
  "Loads a sound given a url."
  [url]
  (let [request (js/XMLHttpRequest.)
        out (atom nil)]
    (.open request "GET" url true)
    (set! (.-responseType request) "arraybuffer")
    (set!
     (.-onload request)
     (fn []
       (.decodeAudioData
        context
        (.-response request)
        (fn [buffer] (reset! out buffer))
        (fn [] (reset! out nil)))))
    (.send request)
    out))

(asset/add-asset :sound load-sound)

(defn play
  "Plays a sound."
  [sound]
  (if-let [buf @sound]
    (let [source (.createBufferSource context)]
      (set! (.-buffer source) buf)
      (.connect source (.-destination context))
      (.start source 0))))
