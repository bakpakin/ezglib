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
  (-draw! [this camera]))
