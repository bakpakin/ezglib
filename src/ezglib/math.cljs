(ns ezglib.math
  (:require [clojure.string :as string]
            [ezglib.protocol :as p]))

;;;;; DEFTYPE

(deftype Matrix ^:no-doc [elements rows cols ^:mutable ta]
  Object
  (toString [_]
            (string/join
             "\n"
             (for [c (range cols)]
               (string/join
                ", "
                (for [r (range rows)] (get-in elements [r c]))))))
  IIndexed
  (-nth [_ n] (get-in elements [(quot n cols) n]))
  (-nth [_ n not-found] (get-in elements [(quot n cols) n] not-found))
  ISeqable
  (-seq [_] (flatten elements))
  IEquiv
  (-equiv [x o] (= elements o))
  p/ITypedArray
  (-typed-array [_] (if ta
                      ta
                      (do
                        (set! ta (js/Float32Array. (to-array (flatten elements))))
                        ta)))
  p/IAdd
  (-add
   [a b]
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
      (.-cols a)
      nil)))
  p/ISubtract
  (-subtract
   [a b]
   (when (and
          a
          b
          (= (.-rows a) (.-rows b))
          (= (.-cols a) (.-cols b)))
     (Matrix.
      (mapv
       #(mapv - %1 %2)
       (.-elements a)
       (.-elements b))
      (.-rows a)
      (.-cols a)
      nil)))
  p/IMultiply
  (-multiply
   [a b]
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
      (.-cols b)
      nil))))

(deftype Vec2 ^:no-doc [x y ^:mutable ta]
  Object
  (toString [_] (str "(" x ", " y ")"))
  p/ITypedArray
  (-typed-array [_] (if ta
                      ta
                      (do
                        (set! ta (js/Float32Array. (array x y)))
                        ta)))
  p/IAdd
  (-add [a b] (Vec2. (+ (.-x a) (.-x b)) (+ (.-y a) (.-y b)) nil))
  p/ISubtract
  (-subtract [a b] (Vec2. (- (.-x a) (.-x b)) (- (.-y a) (.-y b)) nil))
  p/IMultiply
  (-multiply [a b] (if (number? b)
                     (Vec2. (* (.-x a) b) (* (.-y a) b) nil)
                     (Vec2. (* (.-x a) (.-x b)) (* (.-y a) (.-y b)) nil)))
  p/IDivide
  (-divide [a b] (if (number? b)
                   (Vec2. (/ (.-x a) b) (/ (.-y a) b) nil)
                   (Vec2. (/ (.-x a) (.-x b)) (/ (.-y a) (.-y b)) nil)))
  ISeqable
  (-seq [_] (list x y))
  IEquiv
  (-equiv [this o] (if (= (type o) Vec2)
                     (and (identical? x (.-x o)) (identical? y (.-y o)))
                     (= (-seq this) (-seq o))))
  IIndexed
  (-nth [_ n] (case n 0 x 1 y nil))
  (-nth [_ n not-found] (case n 0 x 1 y not-found)))

(deftype Vec3 ^:no-doc [x y z ^:mutable ta]
  Object
  (toString [_] (str "(" x ", " y ", " z ")"))
  p/ITypedArray
  (-typed-array [_] (if ta
                      ta
                      (do
                        (set! ta (js/Float32Array. (array x y z)))
                        ta)))
  p/IAdd
  (-add [a b] (Vec3. (+ (.-x a) (.-x b)) (+ (.-y a) (.-y b)) (+ (.-z a) (.-z b)) nil))
  p/ISubtract
  (-subtract [a b] (Vec3. (- (.-x a) (.-x b)) (- (.-y a) (.-y b)) (- (.-z a) (.-z b)) nil))
  p/IMultiply
  (-multiply [a b] (if (number? b)
                     (Vec3. (* (.-x a) b) (* (.-y a) b) (* (.-z a) b) nil)
                     (Vec3. (* (.-x a) (.-x b)) (* (.-y a) (.-y b)) (* (.-z a) (.-z b)) nil)))
  p/IDivide
  (-divide [a b] (if (number? b)
                   (Vec3. (/ (.-x a) b) (/ (.-y a) b) (/ (.-z a) b) nil)
                   (Vec3. (/ (.-x a) (.-x b)) (/ (.-y a) (.-y b)) (/ (.-z a) (.-z b)) nil)))
  ISeqable
  (-seq [_] (list x y z))
  IEquiv
  (-equiv [this o] (if (= (type o) Vec3)
                     (and (identical? x (.-x o)) (identical? y (.-y o)) (identical? z (.-z o)))
                     (= (-seq this) (-seq o))))
  IIndexed
  (-nth [_ n] (case n 0 x 1 y 2 z nil))
  (-nth [_ n not-found] (case n 0 x 1 y 2 z not-found)))

(deftype Vec4 ^:no-doc [w x y z ^:mutable ta]
  Object
  (toString [_] (str "(" w ", " x ", " y ", " z ")"))
  p/ITypedArray
  (-typed-array [_] (if ta
                      ta
                      (do
                        (set! ta (js/Float32Array. (array w x y z)))
                        ta)))
  p/IAdd
  (-add [a b] (Vec4. (+ (.-w a) (.-w b)) (+ (.-x a) (.-x b)) (+ (.-y a) (.-y b)) (+ (.-z a) (.-z b)) nil))
  p/ISubtract
  (-subtract [a b] (Vec4. (- (.-w a) (.-w b)) (- (.-x a) (.-x b)) (- (.-y a) (.-y b)) (- (.-z a) (.-z b)) nil))
  p/IMultiply
  (-multiply [a b] (if (number? b)
                     (Vec4. (* (.-w a) b) (* (.-x a) b) (* (.-y a) b) (* (.-z a) b) nil)
                     (Vec4. (* (.-w a) (.-w b)) (* (.-x a) (.-x b)) (* (.-y a) (.-y b)) (* (.-z a) (.-z b)) nil)))
  p/IDivide
  (-divide [a b] (if (number? b)
                   (Vec4. (/ (.-w a) b) (/ (.-x a) b) (/ (.-y a) b) (/ (.-z a) b) nil)
                   (Vec4. (/ (.-w a) (.-w b)) (/ (.-x a) (.-x b)) (/ (.-y a) (.-y b)) (/ (.-z a) (.-z b)) nil)))
  ISeqable
  (-seq [_] (list w x y z))
  IEquiv
  (-equiv [this o] (if (= (type o) Vec4)
                     (and (identical? w (.-w o)) (identical? x (.-x o)) (identical? y (.-y o)) (identical? z (.-z o)))
                     (= (-seq this) (-seq o))))
  IIndexed
  (-nth [_ n] (case n 0 w 1 x 2 y 3 z nil))
  (-nth [_ n not-found] (case n 0 w 1 x 2 y 3 z not-found)))

(deftype VecN ^:no-doc [es ^:mutable ta]
  Object
  (toString [_] (str "(" (string/join ", " es) ")"))
  p/ITypedArray
  (-typed-array [_] (if ta
                      ta
                      (do
                        (set! ta (js/Float32Array. (to-array es)))
                        ta)))
  p/IAdd
  (-add [_ b] (if (= (count es) (count (.-es b))) (VecN. (mapv + es (.-es b)) nil)))
  p/ISubtract
  (-subtract [_ b] (if (= (count es) (count (.-es b))) (VecN. (mapv - es (.-es b)) nil)))
  p/IMultiply
  (-multiply [_ b] (if (number? b)
                     (VecN. (mapv #(* b %) es) nil)
                     (if (= (count es) (count (.-es b))) (VecN. (mapv * es (.-es b)) nil))))
  p/IDivide
  (-divide [_ b] (if (number? b)
                   (VecN. (mapv #(/ b %) es) nil)
                   (if (= (count es) (count (.-es b))) (VecN. (mapv / es (.-es b)) nil))))
  ISeqable
  (-seq [_] es)
  IEquiv
  (-equiv [x o] (= (-seq x) (-seq o))))

;;;;; OPERATIONS

(defn add
  "Like +, but works with matricies, vectors, etc."
  ([a]
   a)
  ([a b]
   (if (number? a)
     (+ a b)
     (p/-add a b)))
  ([a b & more]
   (reduce add (add a b) more)))

(defn mult
  "Like *, but works with matricies, vectors, etc."
  ([a]
   a)
  ([a b]
   (if (number? a)
     (* a b)
     (p/-multiply a b)))
  ([a b & more]
   (reduce mult (mult a b) more)))

(defn sub
  "Like -, but works with matricies, vectors, etc."
  ([a]
   (if (number? a)
     (- a)))
  ([a b]
   (if (number? a)
     (- a b)
     (p/-subtract a b)))
  ([a b & more]
   (reduce sub (sub a b) more)))

(defn div
  "Like /, but works with vectors, etc."
  ([a]
   (if (number? a)
     (/ a)
     (p/-inverse a)))
  ([a b]
   (if (number? a)
     (/ a b)
     (p/-divide a b)))
  ([a b & more]
   (reduce div (div a b) more)))

;;;;; MATRIX

(defn m
  "Creates a new matrix. elements should be a nested collection that contains
  matrix elements in column major order."
  [elements]
  (Matrix. (mapv vec elements) (count elements) (count (first elements)) nil))

(defn m-identity
  "Creates an identity matrix."
  ([n]
   (m (partition n (take (* n n) (cycle (cons 1 (repeat n 0)))))))
  ([]
   (m-identity 4)))

(defn m-rows
  "Get the number of rows in a matrix."
  [matrix]
  (.-rows matrix))

(defn m-cols
  "Get the number of columns in a matrix."
  [matrix]
  (.-cols matrix))

(defn m-elements
  "Get the elements of the matrix."
  [matrix]
  (.-elements matrix))

(defn m-transpose
  "Returns the transpose matrix."
  [matrix]
  (Matrix. (apply mapv vector (.-elements matrix)) (.-cols matrix) (.-rows matrix) nil))

;;;; GL UTIL MATRIX FUNCTIONS

(def ^:private ^:no-doc deg-to-rad (/ (.-PI js/Math) 180))

(def m-identity4 ^{:doc "The 4x4 identity matrix."} (m-identity 4))

;Shamelessly stolen from gluPerspective. See http://www.opengl.org/wiki/GluPerspective_code
(defn m-perspective
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
     4
     nil)))

(defn m-ortho
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
   4
   nil))

(defn m-quaternion
  "Constructs a rotation matrix from a quaternion."
  ([w x y z]
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
               [0                       0                       0                       1]] 4 4 nil)))
  ([v4]
   (m-quaternion (.-w v4) (.-x v4) (.-y v4) (.-z v4))))

(defn m-rotate-x
  "Constructs a rotation matrix that represents an x axis rotation."
  [degrees]
  (let [rad (* deg-to-rad degrees)
        c (.cos js/Math rad)
        s (.sin js/Math rad)
        -s (- s)]
    (Matrix. [[1  0  0  0]
              [0  c  s 0]
              [0  -s c  0]
              [0  0  0  1]] 4 4 nil)))

(defn m-rotate-y
  "Constructs a rotation matrix that represents a y axis rotation."
  [degrees]
  (let [rad (* deg-to-rad degrees)
        c (.cos js/Math rad)
        s (.sin js/Math rad)
        -s (- s)]
    (Matrix. [[c  0  -s 0]
              [0  1  0  0]
              [s  0  c  0]
              [0  0  0  1]] 4 4 nil)))

(defn m-rotate-z
  "Constructs a rotation matrix that represents a z axis rotation."
  [degrees]
  (let [rad (* deg-to-rad degrees)
        c (.cos js/Math rad)
        s (.sin js/Math rad)
        -s (- s)]
    (Matrix. [[c  s  0  0]
              [-s c  0  0]
              [0  0  1  0]
              [0  0  0  1]] 4 4 nil)))

(declare v3)

(defn m-translate
  "Constructs a translation matrix."
  ([x y z]
   (Matrix. [[1 0 0 0]
             [0 1 0 0]
             [0 0 1 0]
             [x y z 1]] 4 4 nil))
  ([v]
   (let [v3 (v3 v)] (m-translate (.-x v3) (.-y v3) (.-z v3)))))

(defn m-scale
  "Constructs a scaling matrix."
  ([x y z]
   (Matrix. [[x 0 0 0]
             [0 y 0 0]
             [0 0 z 0]
             [0 0 0 1]] 4 4 nil))
  ([v]
   (let [v3 (v3 v)] (m-scale (.-x v3) (.-y v3) (.-z v3)))))

;;;;; VECTOR

(defn v2
  "Creates a 2d vector from another vector. Extra
  components are ignored."
  [v]
  (if (= (type v) Vec2)
    v
    (if (= (type v) Vec3)
      (Vec2. (.-x v) (.-y v) nil)
      (if (= (type v) VecN)
        (Vec2. (nth (.-es v) 0) (nth (.-es v) 1) nil)
        (Vec2. (.-w v) (.-x v) nil)))))

(defn v3
  "Creates a 3d vector from another vector. If the
  input vector has fewer dimensions, extra components
  default to 0. Extra components are ignored."
  [v]
  (if (= (type v) Vec3)
    v
    (if (= (type v) Vec2)
      (Vec3. (.-x v) (.-y v) 0 nil)
      (if (= (type v) VecN)
        (Vec3. (nth (.-es v) 0) (nth (.-es v) 1) (nth (.-es v) 2) nil)
        (Vec3. (.-w v) (.-x v) (.-y v) nil)))))

(defn v4
  "Creates a 4d vector from another vector. If the
  input vector has fewer dimensions, extra components
  default to 0."
  [v]
  (if (= (type v) Vec4)
    v
    (if (= (type v) Vec3)
      (Vec4. (.-x v) (.-y v) (.-z v) 0 nil)
      (if (= (type v) VecN)
        (Vec4. (nth (.-es v) 0) (nth (.-es v) 1) (nth (.-es v) 2) (nth (.-es v) 2) nil)
        (Vec4. (.-x v) (.-y v) 0 0 nil)))))

(defn v
  "Creates a 2, 3, 4, or n dimensional vector."
  ([elements]
   (VecN. elements nil))
  ([x y]
   (Vec2. x y nil))
  ([x y z]
   (Vec3. x y z nil))
  ([w x y z]
   (Vec4. w x y z nil))
  ([w x y z & more]
   (VecN. (apply vector w x y z more) nil)))

(defn vec2
  "Creates a 2d vector."
  [x y]
  (Vec2. x y nil))

(defn vec3
  "Creates a 3d vector."
  [x y z]
  (Vec3. x y z nil))

(defn vec4
  "Creates a 4d vector."
  [w x y z]
  (Vec4. w x y z nil))

(defn v-dot
  "Returns the vector dot product of two vectors."
  [a b]
  (reduce + (map * a b)))

(defn v-cross
  "Returns the vector cross product of two vectors."
  [a b]
  (let [a (v3 a)
        b (v3 b)
        ax (.-x a)
        ay (.-y a)
        az (.-z a)
        bx (.-x b)
        by (.-y b)
        bz (.-z b)]
    (Vec3.
     (- (* ay bz) (* by az))
     (- (* bx az) (* ax bz))
     (- (* ax by) (* ay bx))
     nil)))

;;;;; GL UTIL

(defn scalexy-rotatez-translatexy
  "Creates a shader uniform from an xy scaling transformation, a z rotation, and an xy translation. Useful for 2d games."
  ([xs ys rz xt yt]
   (let [rad (* deg-to-rad rz)
         c (.cos js/Math rad)
         s (.sin js/Math rad)]
     (js/Float32Array. (array (* c xs) (* s ys) 0 0
                              (* -1 s xs) (* c ys) 0 0
                              0 0 1 0
                              xt yt 0 1))))
  ([f32 xs ys rz xt yt]
   (let [rad (* deg-to-rad rz)
         c (.cos js/Math rad)
         s (.sin js/Math rad)]
     (aset f32 0 (* c xs))
     (aset f32 1 (* s ys))
     (aset f32 12 xt)
     (aset f32 4 (* -1 s xs))
     (aset f32 5 (* c ys))
     (aset f32 13 yt))
   f32))
