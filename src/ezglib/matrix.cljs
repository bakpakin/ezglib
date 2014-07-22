(ns ezglib.matrix
  (:require [clojure.string :as string]))

(defn size
  "Returns the size of the matrix, either 2, 3, or 4."
  [matrix]
  (.-matrixSize matrix))

(defn to-string
  "Returns a string representation of a matrix."
  [matrix]
  (let [s (size matrix)
        r (range s)]
    (string/join
     "\n"
     (for [x r]
      (string/join
       ", "
       (for [y r] (aget matrix (+ x (* y s)))))))))

(extend-type js/Float32Array
  ISeqable
  (-seq [this] (array-seq this)))

(defn- partioned-array-view
  [float32array size]
  (to-array (for [i (range 0 (alength float32array) size)]
              (js/Float32Array. (.-buffer float32array) (* 4 i) size))))

(defn create
  "Creates a matrix, represented as Float32Array."
  [& values]
  (let [arr (to-array values)
        m (js/Float32Array. arr)
        s (.floor js/Math (.sqrt js/Math (alength m)))]
    (set! (.-matrixSize m) s)
    (set! (.-partitionedMatrix m) (partioned-array-view m s))
    m))

(defn matrix?
  "Checks if x is a matrix."
  [x]
  (and (= (type x) js/Float32Array) (.-matrixSize x)))

(defn i
  "Creates an identity matrix of size n."
  ([n]
   (apply create (take (* n n) (cycle (cons 1 (repeat n 0))))))
  ([]
   (i 4)))

(defn dup
  "Duplicates a matrix."
  [matrix]
  (let [c (js/Float32Array. (alength matrix))]
    (doseq [i (range (alength matrix))]
      (aset c i (aget matrix i)))
    (set! (.-matrixSize c) (.-matrixSize matrix))
    c))

(defn add!
  "Adds matricies together. Returns the first argument, which is
  modified to contain the sum. Returns nil if matricies aren't
  the same size, or arguments are invalid."
  ([a]
   a)
  ([a b]
   (when (and (matrix? a) (matrix? b) (= (alength a) (alength b)))
     (doseq [i (range (alength a))]
       (aset a i (+ (aget a i) (aget b i))))
     a))
  ([a b & more]
   (reduce add! (add! a b) more)))

(defn add
  "Adds matricies together."
  ([a]
   (dup a))
  ([a b]
   (add! (dup a) b))
  ([a b & more]
   (apply add! (dup a) b more)))

(defn- nested-for
  [f x y]
  (map (fn [a]
         (map (fn [b]
                (f a b)) y))
       x))

(defn- matrix-mult
  [a b]
  (nested-for
   (fn [x y] (reduce + (map * x y)))
   (.-partitionedMatrix a)
   (apply map array (.-partitionedMatrix b))))

(defn mult!
  "Multiplies matricies together. Returns the first argument, which
  is modified to contain the product. Returns nil if matricies aren't
  the same size, or arguments are invalid."
  ([a]
   a)
  ([a b]
   (when (and (matrix? a) (matrix? b) (= (alength a) (alength b)))
     (let [result (flatten (matrix-mult a b))]
       (doseq [i (range (alength a))]
         (aset a i (nth result i))))
   a))
  ([a b & more]
   (reduce mult! (mult! a b) more)))

(defn mult
  "Multiplies matricies together."
  ([a]
   (dup a))
  ([a b]
   (mult! (dup a) b))
  ([a b & more]
   (apply mult! (dup a) b more)))

;Shamelessly stolen from gluPerspective. See http://www.opengl.org/wiki/GluPerspective_code
(defn perspective
  "Constructs a perspective projection matrix."
  ([left right bottom top z-near z-far]
   (let [tmp (* 2)
         tmp2 (- right left)
         tmp3 (- top bottom)
         tmp4 (- z-far z-near)]
      (create
       (/ tmp tmp2)
       0
       0
       0
       0
       (/ tmp tmp3)
       0
       0
       (/ (+ right left) tmp2)
       (/ (+ top bottom) tmp3)
       (/ (+ z-near z-far) tmp4 -1)
       -1
       0
       0
       (/ (* tmp z-far -1) tmp4)
       0)))
  ([fov aspect z-near z-far]
   (let [ymax (* z-near (.tan js/Math (/ (* fov (.-PI js/Math)) 360)))
         xmax (* ymax aspect)]
     (perspective (- xmax) xmax (- ymax) ymax az-near z-far))))

(defn orthographic
  "Constructs an orthographic projection matrix."
  [x1 x2 y1 y2 z1 z2]
  ())
