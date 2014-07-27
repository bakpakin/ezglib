(ns ezglib.game)

(defmacro defgame
  "Defs an ezglib game."
  [name & {:keys [width height element element-id game-id modes mode canvas
                 assets on-load load-update start-on-load? preload]}]
  (let [pre (if preload `(~preload) `(fn [] nil))
        onld (if on-load
               (if start-on-load?
                 '(fn []
                    (~on-load)
                    (ezglib.game/main-loop! ~name))
                 on-load)
               (if start-on-load?
                 `(fn [] (ezglib.game/main-loop! ~name))
                 `(fn [] nil)))
        ast (if assets
              `(ezglib.asset/load!
                :game ~name
                :assets ~assets
                :update ~load-update
                :on-load ~onld))]
    `(do
       (declare ~name)
       (let [~'canvas (or ~canvas (.createElement js/document "canvas"))
             ~'gl (ezglib.gl/create-context ~'canvas)
             ~'audio-context (ezglib.sound/create-context)
             e# (if ~element-id
                  (.getElementById js/document ~element-id)
                  (if ~element
                    ~element
                    (.-body js/document)))]
         (def ~name {:modes (atom nil)
                    :mode (atom (or ~mode :default))
                    :element e#
                    :loop (atom true)
                    :canvas ~'canvas
                    :event-queue (atom cljs.core.PersistentQueue.EMPTY)
                    :gl ~'gl
                    :audio-context ~'audio-context
                    :dt (atom 0)
                    :now (atom (.getTime (js/Date.)))})
         (.appendChild e# ~'canvas)
         (set! (.-id ~'canvas) ~game-id)
         (set! (.-width ~'canvas) ~width)
         (set! (.-height ~'canvas) ~height)
         (ezglib.gl/reset-viewport! ~'gl)
         (ezglib.input/init! ~name)
         (ezglib.gl/clear! ~'gl)
         ~pre
         (reset! (:modes ~name) (or ~modes {:default (ezglib.game/mode)}))
         ~ast
         ~name))))
