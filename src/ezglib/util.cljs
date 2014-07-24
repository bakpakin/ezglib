(ns ezglib.util)

(defn log
  "Logs messages to the console."
  [& messages]
  (.log js/console (apply str messages)))

(defn now
  "Gets the current time in milliseconds."
  []
  (.getTime (js/Date.)))
