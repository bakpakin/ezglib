(ns ezglib.systems
  (:require [ezglib.math :as m]
            [ezglib.gl :as gl]
            [ezglib.protocol :as p]
            [ezglib.render :as r]
            [ezglib.util :as util]
            [ezglib.game :as game]
            [ezglib.ecs :as ecs :refer [system matcher prop set-prop! swap-prop!]]))

;;;;; DEFAULT SYSTEMS ;;;;;

(defn matrix-system
  [game]
  (system
   (matcher [:ezglib.ecs/root])
   (fn cb
     ([e]
      (cb e m/m-identity4)
      e)
     ([e xform]
      (let [local-xform (or (prop e :local-transform) m/m-identity4)]
        (set-prop! e :global-transform (m/mult local-xform xform))
        (doseq [c (.-children e)]
          (cb c (prop e :global-transform))))))))

(let [origin (m/vec3 0 0 0)
      i2 (m/vec2 1 1)]
  (defn transform2d-system
    [game]
    (system
     (matcher [] [] [:position :rotation :scale :offset])
     (fn [e]
       (let [p (or (prop e :position) origin)
             r (or (prop e :rotation) 0)
             s (or (prop e :scale) i2)]
         (set-prop! e :local-transform (m/mult
                                        (m/m-scale s)
                                        (m/m-rotate-z r)
                                        (m/m-translate p))))))))

(defn render-system
  [game]
  (let [gl (:gl game)
        shader (r/texture-shader gl)
        proj (m/m-ortho 0 (:width game) (:height game) 0 -1000000 1000000)
        color (m/v 1 1 1 1)]
    (system
     (matcher [:drawable])
     (fn [e]
       (let [m (or (prop e :global-transform) m/m-identity4)
             d (prop e :drawable)]
         (gl/set-uniform! gl :modelViewMatrix m)
         (p/-draw! d)))
     (fn []
       (gl/clear! gl)
       (gl/use-shader! gl shader
                       :uniforms {:projectionMatrix proj
                                  :color color
                                  :tDiffuse 0})))))

(defn movement2d-system
  [game]
  (system
   (matcher [:velocity])
   (fn [e]
     (if (not (prop e :position)) (set-prop! e :position (m/v 0 0 0)))
     (swap-prop! e :position m/add (m/mult (prop e :velocity) (game/delta game)))
     nil)))
