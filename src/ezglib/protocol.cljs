(ns ezglib.protocol)

;;;;; GL ;;;;;

(defprotocol ITypedArray
  "Types that extend the ITypedArray protocol can be converted
  to javascript typed arrays for in use in shader uniforms."
  (-typed-array [this]))

;;;;; 3D ;;;;;

(defprotocol ICamera
  (-matrix [this]))

(defprotocol IDrawable
  (-draw! [this]))

(defprotocol IPosition
  (-position [this] "Returns the position of the object as [x y z]."))

(defprotocol IRotation
  (-euler-rotation [this] "Returns the rotation of the object as [x y z angle], where x, y, and z
                   represent the components of the vector of rotation, and angle is the angle rotated about that vector.")
  (-quaternion-rotation [this] "Returns the rotation of the object as [w x y z]."))
