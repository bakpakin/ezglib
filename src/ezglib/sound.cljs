;Because the google closure compiler doesn't have
;have externs for web audio, we have to use hackery
;to use web audio functions. I am reluctant to use my
;own externs, as they pollute the consumer's project.clj.
;The compiler actually seems to have pretty good externs for
;web audio in the source, but it just doesn't use them. Its a shame.
;
;For properties I don't want munged, I use (aget object "property") instead of
;(.-property object)
;
;For methods I don't want munged, I use (.call (aget object "method") object & args) instead of
;(.method object & args)
;
;Hopefully, I can somehow get rid of this ugly workaround.

(ns ezglib.sound
  (:require [ezglib.asset :as asset]
            [ezglib.util :as util]))

(defn create-context
  "Creates an audio context."
  []
    ;would rather use (js/AudioContext.)
    (js* "new AudioContext()"))

(defn- load-sound
  "Loads a sound given a url."
  [game url]
  (let [context (:audio-context game)
        request (js/XMLHttpRequest.)
        out (atom nil)]
    (.open request "GET" url true)
    (set! (.-responseType request) "arraybuffer")
    (set!
     (.-onload request)
     (fn []
       ;would rather use .decodeAudioData
       (.call (aget context "decodeAudioData")
        context
        (.-response request)
        (fn [buffer] (reset! out buffer))
        (fn [] (reset! out nil)))))
    (.send request)
    out))

(defn- sound-loaded?
  [game sound]
  (when @sound
    (let [context (:audio-context game)
          s @sound]
      (set! (.-context s) context)
      s)))

(asset/add-asset
 :asset :sound
 :load-fn load-sound
 :is-done? sound-loaded?)

(defn play
  "Plays a sound."
  [sound]
  (let [context (.-context sound)
        source (.call (aget context "createBufferSource") context)]
    (aset source "buffer" sound)
    (.call (aget source "connect") source (aget context "destination"))
    (.call (aget source "start") source 0)))
