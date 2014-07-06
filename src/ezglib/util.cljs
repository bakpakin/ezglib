(ns ezglib.util)

(defn log
  "Logs messages to the console."
  [& messages]
  (.log js/console (apply str messages)))
