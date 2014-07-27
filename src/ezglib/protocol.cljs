(ns ezglib.protocol)

;;;;; MATH ;;;;;

(defprotocol IAdd
  (-add [this other]))

(defprotocol IMultiply
  (-multiply [this other]))

(defprotocol ISubtract
  (-subtract [this other]))

(defprotocol IDivide
  (-divide [this other]))

(defprotocol IInverse
  (-inverse [this]))

;;;;; GL ;;;;;

(defprotocol ITypedArray
  "Types that extend the ITypedArray protocol can be converted
  to javascript typed arrays for in use in shader uniforms."
  (-typed-array [this]))

;;;;; 3D ;;;;;

(defprotocol I3D
  (-matrix [this]))

(defprotocol IDrawable
  (-draw! [this] "Types that implement -draw should assume
          That the shader and projection matrix have
          already been applied. Textures, attributes, and other uniforms may be
          applied as needed. Implementations should then use ezglib.gl.draw-arrays! or
          ezglib.gl.draw-elements! to draw to the drawing buffer."))

(defprotocol IPosition
  (-position [this] "Returns the position of the object as [x y z]."))

(defprotocol IRotation
  (-euler-rotation [this] "Returns the rotation of the object as [x y z angle], where x, y, and z
                   represent the components of the vector of rotation, and angle is the angle rotated about that vector.")
  (-quaternion-rotation [this] "Returns the rotation of the object as [w x y z]."))
