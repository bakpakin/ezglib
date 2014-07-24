(ns ezglib.matrix
  (:require [clojure.string :as string]
            [ezglib.render.gl :as gl]))

;;;;; DEFTYPE ;;;;;

(deftype Matrix [elements rows cols]
  Object
  (toString [_]
            (string/join
             "\n"
             (for [c (range cols)]
               (string/join
                ", "
                (for [r (range rows)] (get-in elements [r c]))))))

  ICounted
  (-count [_]
          (* rows cols))

  IIndexed
  (-nth [_ n]
        (get-in elements [(quot n cols) n]))
  (-nth [_ n not-found]
        (get-in elements [(quot n cols) n] not-found))

  ISeqable
  (-seq [_]
        (flatten elements))

  gl/ITypedArray
  (-typed-array [_]
                (gl/float32 (flatten elements))))

;;;;; FUNCTIONS ;;;;;

(defn matrix
  "Creates a new matrix. elements should be a nested collection that contains
  matrix elements in column major order."
  [elements]
  (Matrix. (mapv vec elements) (count elements) (count (first elements))))

(defn i
  "Creates an identity matrix."
  ([n]
   (matrix (partition n (take (* n n) (cycle (cons 1 (repeat n 0)))))))
  ([]
   (i 4)))

(defn rows
  "Get the number of rows in a matrix."
  [matrix]
  (.-rows matrix))

(defn cols
  "Get the number of columns in a matrix."
  [matrix]
  (.-cols matrix))

(defn elements
  "Get the elements of the matrix."
  [matrix]
  (.-elements matrix))

(defn transpose
  "Returns the transpose matrix."
  [matrix]
  (Matrix. (apply mapv vector (.-elements matrix)) (.-cols matrix) (.-rows matrix)))

(defn add
  "Adds matricies of the same dimensions together. If
  matricies don't have matching dimensions, nil is returned."
  ([a]
   a)
  ([a b]
   (when (and
          a
          b
          (= (.-rows a) (.-rows b))
          (= (.-cols a) (.-cols b)))
     (Matrix.
      (mapv
       #(mapv + %1 %2)
       (.-elements a)
       (.-elements b))
      (.-rows a)
      (.-cols a))))
  ([a b & more]
   (reduce add (add a b) more)))

(defn mult
  "Multiplies matricies together. If matricies don't have
  compatible dimensions, returns nil."
  ([a]
   a)
  ([a b]
   (when (and
          a
          b
          (= (.-rows a) (.-cols b))
          (= (.-rows b) (.-cols a)))
     (Matrix.
      ((fn
         [f x y]
         (mapv
          (fn
            [a]
            (mapv
             (fn [b]
               (f a b))
             y))
          x))
       (fn
         [x y]
         (reduce + (map * x y)))
       (.-elements a)
       (apply mapv vector (.-elements b)))
      (.-rows a)
      (.-cols b))))
  ([a b & more]
   (reduce mult (mult a b) more)))

;;;; GL UTIL FUNCTIONS ;;;;;

(def ^:private deg-to-rad (/ (.-PI js/Math) 180))

;Shamelessly stolen from gluPerspective. See http://www.opengl.org/wiki/GluPerspective_code
(defn perspective
  "Constructs a perspective projection matrix."
  [fov aspect z-near z-far]
  (let [uh (/ (.tan js/Math (* 0.5 fov deg-to-rad)))
        uw (/ uh aspect)]
    (Matrix.
     [[uw 0 0 0]
      [0 uh 0 0]
      [0 0 (/ z-far (- z-far z-near)) 1]
      [0 0 (/ (* z-far z-near -1) (- z-far z-near)) 0]]
     4
     4)))

(defn ortho
  "Constructs an orthographic projection matrix."
  [x1 x2 y1 y2 z1 z2]
  (Matrix.
   [[(/ 2 (- x2 x1))
     0
     0
     0]
    [0
     (/ 2 (- y2 y1))
     0
     0]
    [0
     0
     (/ -2 (- z2 z1))
     0]
    [(/ (+ x2 x1) (- x2 x1) -1)
     (/ (+ y2 y1) (- y2 y1) -1)
     (/ (+ z2 z1) (- z2 z1) -1)
     1]]
   4
   4))

(defn quaternion
  "Constructs a rotation matrix from a quaternion."
  [w x y z]
  (let [xx (* x x)
        xy (* x y)
        xz (* x z)
        xw (* x w)

        yy (* y y)
        yz (* y z)
        yw (* y w)

        zz (* z z)
        zw (* z w)]
    (Matrix. [[(- 1 (* 2 (+ yy zz)))   (* 2 (- xy zw))         (* 2 (+ xz yw))         0]
              [(* 2 (+ xy zw))         (- 1 (* 2 (+ xx zz)))   (* 2 (- yz xw))         0]
              [(* 2 (- xz yw))         (* 2 (+ yz xw))         (- 1 (* 2 (+ xx yy)))   0]
              [0                       0                       0                       1]] 4 4)))

(defn rotate-x
  "Constructs a rotation matrix that represents an x axis rotation."
  [degrees]
  (let [rad (* deg-to-rad degrees)
        c (.cos js/Math rad)
        s (.sin js/Math rad)
        -s (- s)]
    (Matrix. [[1  0  0  0]
              [0  c  s 0]
              [0  -s c  0]
              [0  0  0  1]] 4 4)))

(defn rotate-y
  "Constructs a rotation matrix that represents a y axis rotation."
  [degrees]
  (let [rad (* deg-to-rad degrees)
        c (.cos js/Math rad)
        s (.sin js/Math rad)
        -s (- s)]
    (Matrix. [[c  0  -s 0]
              [0  1  0  0]
              [s  0  c  0]
              [0  0  0  1]] 4 4)))

(defn rotate-z
  "Constructs a rotation matrix that represents a z axis rotation."
  [degrees]
  (let [rad (* deg-to-rad degrees)
        c (.cos js/Math rad)
        s (.sin js/Math rad)
        -s (- s)]
    (Matrix. [[c  s  0  0]
              [-s c  0  0]
              [0  0  1  0]
              [0  0  0  1]] 4 4)))

(defn translate
  "Constructs a translation matrix."
  [x y z]
  (Matrix. [[1 0 0 0]
            [0 1 0 0]
            [0 0 1 0]
            [x y z 1]] 4 4))

(defn scale
  "Constructs a scaling matrix."
  [x y z]
  (Matrix. [[x 0 0 0]
            [0 y 0 0]
            [0 0 z 0]
            [0 0 0 1]] 4 4))

