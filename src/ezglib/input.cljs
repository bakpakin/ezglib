(ns ezglib.input
  (:require [ezglib.event :as event]))

(def ^:private downkeys (atom {}))

(def ^:private pressedkeys (atom {}))

(def ^:private releasedkeys (atom {}))

(defn key-down?
  "Checks if the given key is down. k should be
  the keyword representation of the key, like :a, :b, :space, :up, etc."
  [k]
  (contains? @downkeys k))

(defn key-pressed?
  "Checks if the given key has been pressed. k should be
  the keyword representation of the key, like :a, :b, :space, :up, etc."
  [k]
  (contains? @pressedkeys k))

(def ^:private code->keys
  {8 :backspace
   9 :tab
   13 :return
   16 :shift
   17 :ctrl
   18 :alt
   19 :pausebreak
   20 :capslock
   27 :escape
   32 :space
   33 :pageup
   34 :pagedown
   35 :end
   36 :home
   37 :left
   38 :up
   39 :right
   40 :down
   43 :+
   44 :printscreen
   45 :insert
   46 :delete
   48 :0
   49 :1
   50 :2
   51 :3
   52 :4
   53 :5
   54 :6
   55 :7
   56 :8
   57 :9
   59 :semi-colon
   61 :=
   65 :a
   66 :b
   67 :c
   68 :d
   69 :e
   70 :f
   71 :g
   72 :h
   73 :i
   74 :j
   75 :k
   76 :l
   77 :m
   78 :n
   79 :o
   80 :p
   81 :q
   82 :r
   83 :s
   84 :t
   85 :u
   86 :v
   87 :w
   88 :x
   89 :y
   90 :z
   96 :0
   97 :1
   98 :2
   99 :3
   100 :4
   101 :5
   102 :6
   103 :7
   104 :8
   105 :9
   106 :*
   107 :+
   109 :-
   110 :.
   111 :/
   112 :f1
   113 :f2
   114 :f3
   115 :f4
   116 :f5
   117 :f6
   118 :f7
   119 :f8
   120 :f9
   121 :f10
   122 :f11
   123 :f12
   144 :numlock
   145 :scrolllock
   186 :semi-colon
   187 :=
   188 :comma
   189 :-
   190 :.
   191 :/
   192 :`
   219 :open-bracket
   220 :backslash
   221 :close-bracket
   222 :'})

(defn event-key
  "Gets the key from the keyboard event."
  [ev]
  (code->keys (.-which ev)))

(defn on-key-press!
  "Adds an event handler for key presses."
  [event-mode k fn1]
  (event/add-handler! event-mode [:keypress k] fn1))

(defn on-key-down!
  "Adds an event handler for key presses."
  [event-mode k fn1]
  (event/add-handler! event-mode [:keydown k] fn1))

(defn on-key-up!
  "Adds an event handler for key releases."
  [event-mode k fn1]
  (event/add-handler! event-mode [:keyup k] fn1))

(defn enqueue-keys!
  "Enqueues keyboard events. This should be called once a frame."
  []
  (doseq [[k ev] @pressedkeys]
    (event/enqueue-event! [:keypress k] ev))
  (doseq [[k ev] @downkeys]
    (event/enqueue-event! [:keydown k] ev))
  (doseq [[k ev] @releasedkeys]
    (event/enqueue-event! [:keyup k] ev))
  (swap! downkeys (fn [ks] (apply dissoc ks (keys @releasedkeys))))
  (reset! pressedkeys {})
  (reset! releasedkeys {}))

(defn init!
  "Initializes the input."
  [canvas]
  (set! (.-onclick canvas) (fn [ev] (event/enqueue-event! :click ev)))
   (set! (.-onkeydown js/window) (fn [ev]
                                     (let [k (event-key ev)]
                                       (when (not (contains? @downkeys k))
                                         (swap! pressedkeys assoc k ev)
                                         (swap! downkeys assoc k ev)))))
   (set! (.-onkeyup js/window) (fn [ev]
                                  (let [k (event-key ev)]
                                    (swap! releasedkeys assoc k ev)))))
