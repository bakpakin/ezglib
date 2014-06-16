(ns example.examplea
  (:require [ezglib.core :as ezg]))

;Example on how to define your own assets
(ezg/add-asset :image
  (fn [path] (str "The image found at: " path)))

(ezg/add-asset :sound
  (fn [path] (str "Beep! The sound found at: " path)))

(def gm (ezg/game 600 400 "gameDiv" "game"))

(ezg/load! :image "pic" ["dir/pic.png"])
(ezg/load! :image "pic1" ["dir/pic1.png"])
(ezg/load! :image "pic2" ["dir/pic2.png"])
(ezg/load! :image "pic3" ["dir/pic3.png"])

(ezg/load! :sound "snd" ["dir/snd.png"])
(ezg/load! :sound "snd1" ["dir/snd1.png"])
(ezg/load! :sound "snd2" ["dir/snd2.png"])
(ezg/load! :sound "snd3" ["dir/snd3.png"])

(ezg/asset "pic")
(ezg/asset "pic1")
(ezg/asset "pic2")
(ezg/asset "pic3")

(ezg/asset "snd")
(ezg/asset "snd1")
(ezg/asset "snd2")
(ezg/asset "snd3")

(ezg/free! "pic")

(ezg/free! "snd")

(ezg/free-all-type! :image)

(ezg/free-all-type! :sound)

(ezg/main-loop gm :default)
