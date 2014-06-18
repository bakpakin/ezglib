(ns ezglib.sound
  (:require [ezglib.asset :as asset]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout]])
  (:require-macros [cljs.core.async.macros :as m :refer [go alt!]]))

(defn init!
  "Initializes WebAudio. Returns an AudioContext."
  []
  (try
    (do
      (set! (.-AudioContext js/window) (or (.-AudioContext js/window) (.-webkitAudioContext js/window)))
      (js/AudioContext.))
    (catch js/Object e
      (.log js/console "Your browser does not support WebAudio."))))

(defn load-sound
  "Loads a sound given a url."
  [game url]
  (let [ac (:ac game)
        request (js/XMLHttpRequest.)
        source (.createBufferSource ac)
        out (atom nil)]
    (.open request "GET" url true)
    (set! (.-responseType request) "arraybuffer")
    (set!
     (.-onload request)
     (fn []
       (.decodeAudioData
        ac
        (.-response request)
        (fn [buffer] (reset! out buffer))
        (fn [] (reset! out nil)))))
    (.send request)
    [out game]))

(defn play
  "Plays a sound."
  [sound]
  (let [[buf-atom gm] sound
        buf @buf-atom
        ac (:ac gm)]
    (if buf
      (let [source (.createBufferSource ac)]
        (set! (.-buffer source) buf)
        (.connect source (.-destination ac))
        (.start source 0)))))

(asset/add-asset :sound load-sound)
