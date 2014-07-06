;This namespace provides a wrapper for Sylvester.js
(ns ezglib.math
  (:require [jcoglan.sylvester :as s]))

;;;;; VECTOR ;;;;;

(defn v
  "Creates a new Sylvester Vector."
  [& es]
  (js/$V (apply array es)))

(def v-i (.-i js/Vector))
(def v-j (.-j js/Vector))
(def v-k (.-k js/Vector))
(def v-random (.-Random js/Vector))
(def v-zero (.-Zero js/Vector))

;;;;; MATRIX ;;;;;

(defn m
  "Creates a new Sylvester matrix."
  [& rows]
  (js/$M (apply array (map #(apply array %) rows))))

(defn m-diagonal
  "Returns a square matrix whose leading-diagonal elements are vs, and whose off-diagonal elements are zero."
  [& vs]
  ((.-Diagonal js/Matrix) (apply array vs)))

(def m-i (.-I js/Matrix))
(def m-random (.-Random js/Matrix))
(def m-rotation (.-Rotation js/Matrix))
(def m-rotation-x (.-RotationX js/Matrix))
(def m-rotation-y (.-RotationY js/Matrix))
(def m-rotation-z (.-RotationZ js/Matrix))
(def m-zero (.-Zero js/Matrix))

;;;;; LINE ;;;;;

(defn l
  "Creates a new Sylvester line."
  [anchor direction]
  (js/$L
   (if (vector? anchor)
     (v anchor)
     anchor)
   (if (vector? direction)
     (v direction)
     direction)))

(def l-x (.-X js/Line))
(def l-y (.-Y js/Line))
(def l-z (.-Z js/Line))

;;;;; PLANE ;;;;;

(defn p
  "Creates a new Sylvester plane."
  ([anchor normal]
   (js/$P anchor normal))
  ([anchor v1 v2]
   (js/$P anchor v1 v2)))

(def p-xy (.-XY js/Plane))
(def p-xz (.-XZ js/Plane))
(def p-yx (.-YX js/Plane))
(def p-yz (.-YZ js/Plane))
(def p-zx (.-ZX js/Plane))
(def p-zy (.-ZY js/Plane))


























