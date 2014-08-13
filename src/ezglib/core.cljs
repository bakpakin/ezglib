(ns ezglib.core
  (:require [ezglib.util :as util]
            [ezglib.gl :as gl]
            [ezglib.protocol :as p]
            [ezglib.math :as m]
            [tailrecursion.priority-map :as pm :refer [priority-map]]))

;;;;; ASSET LOADING

(def ^:private ^:no-doc loaders (atom {}))

(def ^:private ^:no-doc is-dones (atom {}))

(def ^:private ^:no-doc releasers (atom {}))

(defn asset-group
  "Creates a new asset group."
  []
  {:id-asset (atom {})
   :id-args (atom {})
   ;args-id is a nested map
   :args-id (atom {})})

(def ^:private ^:no-doc default-group (asset-group))

(defn asset
  "Checks if the given asset has been loaded. If so, returns it - else
  returns nil."
  ([id]
   (asset default-group id))
  ([asset-group id]
   (get (get @(:id-asset asset-group) id) 1)))

(defn- ^:no-doc make-handles
  [game args]
  (let [f (fn [[atype id & args]]
            (let [h (apply (@loaders atype) game args)
                  isdn (@is-dones atype)
                  akey (vec args)
                  bkey (conj akey atype)]
              [bkey [h isdn id atype akey]]))
        ret (apply hash-map (mapcat f args))]
    ret))

(defn load!
  "Loads assets async, and binds them to identifiers for later use.
  Loaded assets can be retrieved via ezglib.core/asset. load!
  returns an atom that encapsulates
  the progress of loading as a number between 0 and 1.

  load! requires an instance of game (created via ezglib.core/game),
  and takes variadic arguments in map form.

  :assets - a vector of vectors representing the assets to load.
  Should take the form [[:asset-type new-asset-id & loading-arguments]].
  For example, (load! game
  :assets [[:sound :beep \"assets/beep.wav\"]
  [:sound :click \"assets/click.wav\"]])
  will load two sounds and store them as assets named :click and :beep.

  :asset-group (optional) - the asset group to load the asset into. Asset groups
  can be used to organize assets.

  :update (optional) - a function that's called every frame while the loading
  hasn't finished. Takes one parameter, the progress as a value from 0 to 1.

  :on-load (optional) - a callback function that is called when loading is finished."
  ([game & args]
   (let [mp (apply hash-map args)
         type-id-args (or (mp :assets) [])
         on-load (or (mp :on-load) (fn [] nil))
         asset-group (or (mp :asset-group) default-group)
         update (or (mp :update) (fn [p] nil))
         progress (atom 0)
         done-handles (atom 0)
         handles (atom (make-handles game type-id-args))
         num-handles (count @handles)
         f (fn cb [ts]
             (update @progress)
             (if (seq @handles)
               (let [finished-handles (select-keys @handles (for [[k [h isdn id atype akey]] @handles :when (isdn game h)] k))]
                 (doseq [[bkey [h isdn id atype akey]] finished-handles]
                   (swap! (:args-id asset-group) assoc-in [atype akey] id)
                   (swap! (:id-args asset-group) assoc id bkey)
                   (swap! (:id-asset asset-group) assoc id [game (isdn game h)])
                   (util/log "Loaded " (name atype) " with id \"" id "\" from " akey ".")
                   (swap! done-handles inc)
                   (reset! progress (/ @done-handles num-handles)))
                 (reset! handles (apply dissoc @handles (keys finished-handles)))
                 (js/requestAnimationFrame cb))
               (on-load)))]
     (js/requestAnimationFrame f)
     progress)))

(defn free!
  "Frees already loaded assets."
  ([asset-group ids]
   (doseq [id ids]
     (when-let [[game a] (@(:id-asset asset-group) id)]
       (let [bkey (@(:id-args asset-group) id)
             asset-type (peek bkey)
             akey (pop bkey)]
         ((@releasers asset-type) game a)
         (swap! (:args-id asset-group) assoc asset-type (dissoc (@(:args-id asset-group) asset-type) akey))
         (swap! (:id-asset asset-group) dissoc id)
         (swap! (:id-args asset-group) dissoc id)
         nil))))
  ([ids]
   (free! default-group ids)))

(defn free-all-type!
  "Frees all assets of a single type."
  ([asset-group asset-type]
   (doseq [id (vals (@(:args-id asset-group) asset-type))]
     (free! asset-group id)))
  ([asset-type]
   (free-all-type! default-group asset-type)))

(defn free-all!
  "Frees all assets of one type, or all
  assets if no type is specified. When called with no
  arguments, will only free the default asset group."
  ([asset-group]
   (doseq [at (keys @(:args-id asset-group))]
     (free-all-type! asset-group at)))
  ([]
   (free-all! default-group)))

(defn add-asset
  "Adds functionality for loading and freeing assets. Takes named parameters.
  \n
  :asset - the name of the asset to define, should be a keyword.\n
  :load-fn - fn that takes at least one argument, the game, and any number of other arguments.
  Returns a handle to the loading process.\n
  :is-done? (optional) - fn to check if the loading is done (should take two
  parameters, the game and the asset handle.)
  If so, should return either the original handle or a new handle to the asset.
  Default value is (fn [game x] x).\n
  :free-fn (optional) - fn that frees the asset from memory (takes two
  parameters, the game and the asset handle).
  free-fn should take the same parameter that is-fn returns.
  Default value is (fn [game x] nil)."
  [& {:keys [asset load-fn is-done? free-fn]}]
  (swap! loaders assoc asset load-fn)
  (swap! releasers assoc asset (or free-fn (fn [game x] nil)))
  (swap! is-dones assoc asset (or is-done? (fn [game x] x)))
  nil)

;;;;; SOUND

(defn create-audio-context
  "Creates an audio context."
  []
  (let [c (or (aget js/window "AudioContext") (aget js/window "webkitAudioContext"))]
    (c.)))

(defn play!
  "Plays a sound."
  [sound]
  (let [context (.-context sound)
        source (.call (aget context "createBufferSource") context)]
    (aset source "buffer" sound)
    (.call (aget source "connect") source (aget context "destination"))
    (.call (aget source "start") source 0)))

(defn- ^:no-doc load-sound
  "Loads a sound given a url."
  [game url]
  (let [context (:audio-context game)
        request (js/XMLHttpRequest.)
        out (atom nil)]
    (.open request "GET" url true)
    (set! (.-responseType request) "arraybuffer")
    (set!
     (.-onload request)
     (fn []
       ;would rather use .decodeAudioData
       (.call (aget context "decodeAudioData")
              context
              (.-response request)
              (fn [buffer] (reset! out buffer))
              (fn [] (reset! out nil)))))
    (.send request)
    out))

(defn- ^:no-doc sound-loaded?
  [game sound]
  (when @sound
    (let [context (:audio-context game)
          s @sound]
      (set! (.-context s) context)
      s)))

(add-asset
 :asset :sound
 :load-fn load-sound
 :is-done? sound-loaded?)

;;;;; SHADER

(def ^:private ^:no-doc frag-header
  "
  precision mediump float;\n
  varying vec2 vUv;\n
  uniform sampler2D tDiffuse;\n
  uniform sampler2D tNormal;\n
  uniform vec4 color;\n
  uniform float time;\n
  ")

(def ^:private ^:no-doc color-frag-src
  "void main(void) {\n
  gl_FragColor = color;\n
  }")

(def ^:private ^:no-doc texture-frag-src
  "void main(void) {\n
  gl_FragColor = color * texture2D(tDiffuse, vUv);\n
  }")

(def ^:private ^:no-doc vert-header
  "
  precision mediump float;\n
  attribute vec3 position;\n
  attribute vec2 aUv;\n
  varying vec2 vUv;\n
  uniform mat4 projectionMatrix;\n
  uniform mat4 modelViewMatrix;\n
  uniform float time;\n
  ")

(def ^:private ^:no-doc default-vert-src
  "void main(void) {\n
  gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);\n
  vUv = aUv;\n
  }")

(defn ^:private ^:no-doc load-shader-string
  [game frag vert]
  [(atom nil) (atom frag) (atom vert)])

(defn ^:private ^:no-doc load-shader-file
  [game frag vert]
  (let [gl (:gl game)
        rf (js/XMLHttpRequest.)
        rv (js/XMLHttpRequest.)
        atm-f (atom nil)
        atm-v (atom nil)]
    (.open rf "GET" frag true)
    (.open rv "GET" vert true)
    (set! (.-onload rf) #(reset! atm-f (.-response rf)))
    (set! (.-onload rv) #(reset! atm-v (.-response rv)))
    (.send rf)
    (.send rv)
    [(atom nil) atm-f atm-v]))

(defn ^:private ^:no-doc load-shader
  ([game load-type frag vert]
   (case load-type
     :string (load-shader-string game frag vert)
     :file (load-shader-file game frag vert)))
  ([game frag vert]
   (load-shader-string game frag vert)))

(defn ^:private ^:no-doc shader-loaded?
  [game [shader-atm frag-atm vert-atm]]
  (if-let [shader @shader-atm]
    shader
    (if (and @frag-atm @vert-atm)
      (reset! shader-atm (gl/load-shader (:gl game) (str frag-header "\n" @frag-atm) (str vert-header "\n" @vert-atm))))))

(defn ^:no-doc free-shader
  [game shader]
  (gl/free-shader (:gl game) shader))

(add-asset
 :asset :shader
 :load-fn load-shader
 :free-fn free-shader
 :is-done? shader-loaded?)

(defn texture-shader
  "Creates an ezglib texture shader for a gl context."
  [gl]
  (gl/load-shader gl (str frag-header texture-frag-src) (str vert-header default-vert-src)))

;;;;; TEXTURE

(defn ^:private ^:no-doc load-texture
  ([game url min-filter mag-filter mipmap?]
   (let [atm (atom nil)
         image (js/Image.)]
     (set! (.-src image) url)
     (set! (.-crossOrigin image) "anonymous")
     (set! (.-onload image) (fn []
                              (reset! atm (gl/load-texture (:gl game) image min-filter mag-filter mipmap?))))
     atm))
  ([game url min-filter mag-filter]
   (load-texture game url min-filter mag-filter false))
  ([game url]
   (load-texture game url gl/linear gl/linear false)))

(defn ^:private ^:no-doc free-texture
  [game texture]
  (gl/free-texture (:gl game) texture))

(defn ^:private ^:no-doc texture-loaded?
  [game tex-atm]
  (if-let [tex @tex-atm] tex))

(add-asset
 :asset :texture
 :load-fn load-texture
 :free-fn free-texture
 :is-done? texture-loaded?)

;;;;; CAMERAS

(deftype ^{:no-doc true} Camera2D [x y hw hh angle pos matrix]
  p/ITypedArray
  (-typed-array [_] (p/-typed-array matrix)))

(defn camera-2d
  "Constructs a 2D camera."
  ([x y hw hh]
   (camera-2d x y hw hh 0))
  ([x y hw hh angle]
   (Camera2D.
    x
    y
    hw
    hh
    angle
    (m/Vec3. x y 0 nil)
    (m/mult
     (m/ortho (- x hw) (+ x hw) (- y hh) (+ y hh) -1000000 1000000)
     (m/rotate-z angle)))))

;;;;; SPRITE

(deftype ^:no-doc Sprite [gl tex verts uv]
  Object
  (toString [_] (str "Sprite"))
  p/IDrawable
  (-draw! [_]
          (gl/bind-texture! gl tex)
          (gl/set-attribute*! gl :position verts)
          (gl/set-attribute*! gl :aUv uv)
          (gl/draw-arrays! gl gl/triangle-strip 4)))

(defn sprite
  "Creates a new sprite from a texture."
  [tex]
  (let [w (.-imageWidth tex)
        h (.-imageHeight tex)
        u (.-uvWidth tex)
        v (.-uvHeight tex)]
    (Sprite.
     (.-gl tex)
     tex
     (gl/buffer (.-gl tex)
                :data (js/Float32Array. (array 0 0 0 0 h 0 w 0 0 w h 0))
                :item-size 3)
     (gl/buffer (.-gl tex)
                :data (js/Float32Array. (array 0 0 0 v u 0 u v))
                :item-size 2))))

(defn ^:private ^:no-doc load-sprite
  ([game url min-filter mag-filter mipmap?]
   (let [atm (atom nil)
         image (js/Image.)]
     (set! (.-src image) url)
     (set! (.-crossOrigin image) "anonymous")
     (set! (.-onload image) (fn []
                              (reset! atm (sprite (gl/load-texture (:gl game) image min-filter mag-filter mipmap?)))))
     atm))
  ([game url min-filter mag-filter]
   (load-sprite game url min-filter mag-filter false))
  ([game url]
   (load-sprite game url gl/linear gl/linear false)))

(defn ^:private ^:no-doc free-sprite
  [game sprite]
  (gl/free-texture (:gl game) (.-tex sprite)))

(defn ^:private ^:no-doc sprite-loaded?
  [game sprite-atm]
  (if-let [sprite @sprite-atm] sprite))

(add-asset
 :asset :sprite
 :load-fn load-sprite
 :free-fn free-sprite
 :is-done? sprite-loaded?)

;;;;; TEXT

(def ^:private ^:no-doc font-canvas (.createElement js/document "canvas"))
(def ^:private ^:no-doc font-ctx (.getContext font-canvas "2d"))

(def ^:private ^:no-doc text-div (.createElement js/document "div"))
(def ^:private ^:no-doc div-style (.-style text-div))
(set! (.-position div-style) "absolute")
(set! (.-visibility div-style) "hidden")
(set! (.-width div-style) "auto")
(set! (.-height div-style) "auto")

(defn ^:private ^:no-doc string-size
  "Returns the dimensions of a string as [width height]."
  [font-name font-size string]
  (.appendChild (.-body js/document) text-div)
  (set! (.-font div-style) (str font-size "px " font-name))
  (set! (.-innerHTML text-div) string)
  (let [ret [(.-clientWidth text-div) (.-clientHeight text-div)]]
    (.removeChild (.-body js/document) text-div)
    ret))

(defn ^:private ^:no-doc load-text
  "Loads text for web gl rendering."
  [game font-name font-size text]
  (let [gl (:gl game)
        [w h] (string-size font-name font-size text)]
    (set! (.-width font-canvas) (+ 2 w))
    (set! (.-height font-canvas) (+ 2 h))
    (set! (.-font font-ctx) (str font-size "px " font-name))
    (set! (.-fillStyle font-ctx) "#FFFFFF")
    (set! (.-textBaseline font-ctx) "top")
    (.clearRect font-ctx 0 0 (+ 2 w) (+ 2 h))
    (.fillText font-ctx text 0 0)
    (sprite (gl/load-texture gl font-canvas gl/linear gl/linear false))))

(defn ^:private ^:no-doc free-text
  [game text]
  (gl/free-texture (:gl game) text))

(add-asset
 :asset :text
 :load-fn load-text
 :free-fn free-text)

;;;;; EVENT

(defn enqueue-event!
  "Queues an event that happened in the game."
  [game event-type & params]
  (if (seq (get @(:handlers game) event-type))
    (swap! (:event-queue game) conj [event-type (vec params)])))

(defn ^:private ^:no-doc handle-events!
  "Executes all event handlers in the game-state for the currently queued events."
  [game]
  (enqueue-event! game ::end-update)
  (let [event-queue (:event-queue game)]
    (while (when-let [n (peek @event-queue)] (not (= ::end-update n)))
      (let [[et params] (peek @event-queue)]
        (when-let [hs (@(:handlers game) et)]
          (doseq [h (vals hs)]
            (apply h params)))
        (swap! event-queue pop)))
    ;pop off ::end-update
    (swap! event-queue pop)))

(defn drain!
  "Drains all events in the event queue."
  [game]
  (reset! (:event-queue game) cljs.core.PersistentQueue.EMPTY))

;;;;; STATE FUNCTIONS ;;;;;

(defn add-handler!
  "Adds a handler to the state for a certain event type.
  Returns the handler id."
  [state event-type handler]
  (let [id (swap! (:next-id state) inc)]
    (swap! (:handlers state) assoc-in [event-type id] handler)
    (swap! (:handler-types state) assoc id event-type)
    id))

(defn handler
  "Gets the handler associated with the given id
  in the state."
  [state id]
  (if-let [et (@(:handler-types state) id)]
    (get-in @(:handlers state) [et id])))

(defn remove-handler!
  "Removes a handler from the state by id.
  Returns the handler."
  [state id]
  (if-let [et (@(:handler-types state) id)]
    (let [h (get-in @(:handlers state) [et id])]
      (swap! (:handler-types state) dissoc id)
      (swap! (:handlers state)
             (fn [ets e-t i] (assoc ets e-t (dissoc (ets e-t) i)))
             et
             id)
      h)))

(defn add-state!
  "Adds a state to the game. A state is created via
  ezglib.core/state, and encapsulates an update and render function
  for the game, as well as event handlers. A game state also can contain
  a entity-component-system world, created vie ezglib.core/world."
  [game state-id state]
  (swap! (:states game) assoc state-id state))

(defn remove-state!
  "Removes a state from the game."
  [game state-id]
  (swap! (:states game) dissoc state-id))

(defn current-state-id
  "Gets the id of the current state of the game."
  [game]
  @(:state game))

(defn current-state
  "Gets the current state of the game."
  [game]
  (get @(:states game) @(:state game)))

(defn get-state
  "Retrieves a state by id from the game."
  [game id]
  (@(:states game) id))

(defn set-state!
  "Sets the state of the game. If state-id
  does not exist in the game, does nothing and returns nil. Else
  sets the game state and returns the new state."
  [game state-id]
  (if-let [new-state (@(:states game) state-id)]
    (do
      (reset! (:state game) state-id)
      (reset! (:handlers game) @(:handlers new-state))
      (reset! (:handler-types game) @(:handler-types new-state))
      new-state)
    nil))

(defn states
  "Gets all availble states of the game."
  [game]
  @(:states game))

(declare on-key-press! on-key-release! on-key-down! world)

(defn state
  "Makes a game state with specified handlers. Options:

  :update (optional) - a function called once a frame while this game state is active.

  :render (optional) - a function called after :update and default rendering for custom drawing.

  :handlers (optional) - a map of event-types to handler functions. When
  the event is trigger and the given event-type is pushed to the event-queue, all
  handlers in the game state are called.

  :world (optional) - a world created via ezglib.core/world that encapsulates
  entities, systems, and components.

  :key-press, :key-release, :key-down (optional) - maps of keys as keywords to handlers.
  For example, to log to console when the space bar is pressed, one would add
  the option :key-press {:space (fn [key-event] (.log js/console \"Space Pressed!\"))}"
  [game & {:keys [update render handlers key-press key-release key-down world]}]
  (let [m {:update update
           :world world
           :handlers (atom {})
           :handler-types (atom {})
           :game game
           :render render
           :next-id (atom 0)}]
    (doseq [[k v] handlers]
      (add-handler! m k v))
    (doseq [[k v] key-press]
      (on-key-press! m k v))
    (doseq [[k v] key-release]
      (on-key-release! m k v))
    (doseq [[k v] key-down]
      (on-key-down! m k v))
    m))

;;;;; ENTITY AND SYSTEM

(deftype World [id entities systems-by-priority system-entities]
  Object
  (toString [_] (str "World " id))
  IHash
  (-hash [_] id))

(deftype System [id matcher func one-time-func]
  Object
  (toString [_] (str "System " id))
  IHash
  (-hash [_] id))

(deftype Entity [id ^:mutable changed-in-worlds]
  Object
  (toString [_] (str "Entity " id))
  IHash
  (-hash [_] id))

(let [nid (atom 0)] (defn- uid [] (swap! nid inc)))

(defn id
  "Gets the id of a world, system, or entity."
  [x]
  (.-id x))

(defn entity?
  "Is x an entity?"
  [x]
  (= Entity (type x)))

(defn system?
  "Is x a system?"
  [x]
  (= System (type x)))

(defn world?
  "Is x a world?"
  [x]
  (= World (type x)))

(defn entity
  "Creates a new entity."
  [& properties]
  (loop [e (Entity. (uid) {})
         p (partition 2 properties)]
    (if (empty? p)
      e
      (let [[k v] (first p)]
        (aset e (name k) v)
        (recur e (rest p))))))

(defn matcher
  "Creates a new system matcher. A system matcher matches entities to systems based
  on their properties."
  ([required excluded one-required]
   (fn [e]
     (let [f #(aget e (name %))]
       (and
        (every? f required)
        (not-any? f excluded)
        (some f one-required)))))
  ([required excluded]
   (fn [e]
     (let [f #(aget e (name %))]
       (and
        (every? f required)
        (not-any? f excluded)))))
  ([required]
   (fn [e]
     (let [f #(aget e (name %))]
       (every? f required)))))

(defn system
  "Creates a new system. Systems process entities, which hold the game data."
  ([matcher per-entity-func]
   (System. (uid) matcher per-entity-func (fn [] nil)))
  ([matcher per-entity-func func]
   (System. (uid) matcher per-entity-func func)))

(defn- try-add-entity-to-system!
  [^ World world ^Entity entity ^System system]
  (swap! (.-system-entities world) update-in [system] (if ((.-matcher system) entity) conj disj) entity))

(defn prop
  "Gets a property of an entity."
  [e property]
  (aget e (name property)))

(defn- ^:no-doc xor [a b] (or (and a (not b)) (and (not a) b)))

(defn- ^:no-doc fmap [f m]
  (into {} (for [[k v] m] [k (f v)])))

(defn set-prop!
  "Sets a property of an entity. Returns the new value."
  [e property value]
  (if (xor (prop e property) value)
    (set! (.-changed-in-worlds e) (fmap (fn [x] true) (.-changed-in-worlds e))))
  (aset e (name property) value)
  value)

(defn swap-prop!
  "Similar to swap! on atoms."
  [e property f & args]
  (set-prop! e property (apply f (prop e property) args)))

(defn add-system!
  "Adds a system to the world."
  [world system priority]
  (swap! (.-systems-by-priority world) assoc system priority)
  (swap! (.-system-entities world) assoc system #{})
  (doseq [e @(.-entities world)]
    (try-add-entity-to-system! world e system)))

(defn add-entity!
  "Adds an entity to the world."
  [world entity]
  (swap! (.-entities world) conj entity)
  (set! (.-changed-in-worlds entity) (assoc (.-changed-in-worlds entity) world true)))

(defn remove-entity!
  "Removes an entity from a world. Returns the entity."
  [world entity]
  (swap! (.-entities world) disj entity)
  (swap! (.-system-entities world) (fn [ses] (fmap #(disj % entity) ses)))
  entity)

(defn remove-system!
  "Removes a system from the world. Returns the system."
  [world system]
  (swap! (.-systems-by-priority world) dissoc system)
  (swap! (.-system-entities world) dissoc system)
  system)

(defn add!
  "Adds systems and entities to the world. Returns the world."
  [world & items]
  (let [counter (atom 0)]
    (doseq [i items]
      (if (= (type i) System)
        (add-system! world i (swap! counter inc))
        (add-entity! world i))))
  world)

(defn world
  "Creates a new world."
  [& items]
  (apply add! (World. (uid) (atom #{}) (atom (priority-map)) (atom {})) items))

(defn update!
  "Updates the world."
  [world]
  (doseq [e @(.-entities world)]
    (when (get (.-changed-in-worlds e) world)
      (doseq [s (keys @(.-systems-by-priority world))]
        (try-add-entity-to-system! world e s))
      (set! (.-changed-in-worlds e) (assoc (.-changed-in-worlds e) world false))))
  (doseq [s (keys @(.-systems-by-priority world))]
    ((.-one-time-func s))
    (doseq [e (get @(.-system-entities world) s)]
      ((.-func s) e))))

;;;;; KEYBOARD

(def ^:private ^:no-doc downkeys (atom {}))

(def ^:private ^:no-doc pressedkeys (atom {}))

(def ^:private ^:no-doc releasedkeys (atom {}))

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

(def ^:private ^:no-doc code-to-keys
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
   43 :plus
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
   61 :equals
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
   106 :asterix
   107 :plus
   109 :minus
   110 :period
   111 :forward-slash
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
   187 :equals
   188 :comma
   189 :minus
   190 :period
   191 :forward-slash
   192 :grave-accent
   219 :open-bracket
   220 :backslash
   221 :close-bracket
   222 :quote})

(defn event-key
  "Gets the key from the keyboard event."
  [ev]
  (code-to-keys (aget ev "which")))

(defn on-key-press!
  "Adds an event handler for key presses."
  [state k fn1]
  (add-handler! state [:keypress k] fn1))

(defn on-key-down!
  "Adds an event handler for key presses."
  [state k fn1]
  (add-handler! state [:keydown k] fn1))

(defn on-key-release!
  "Adds an event handler for key releases."
  [state k fn1]
  (add-handler! state [:keyrelease k] fn1))

(defn ^:no-doc enqueue-keys!
  "Enqueues keyboard events. This should be called once a frame."
  [game]
  (doseq [[k ev] @pressedkeys]
    (enqueue-event! game [:keypress k] ev))
  (doseq [[k ev] @downkeys]
    (enqueue-event! game [:keydown k] ev))
  (doseq [[k ev] @releasedkeys]
    (enqueue-event! game [:keyrelease k] ev))

  (when (seq @pressedkeys)
    (enqueue-event! game :keypress))
  (when (seq @downkeys)
    (enqueue-event! game :keydown))
  (when (seq @releasedkeys)
    (enqueue-event! game :keyrelease))

  (swap! downkeys (fn [ks] (apply dissoc ks (keys @releasedkeys))))
  (reset! pressedkeys {})
  (reset! releasedkeys {}))

;;;;; GLOBAL KEYBOARD SETUP

(.addEventListener js/window "keydown" (fn [ev]
                                         (let [k (event-key ev)]
                                           (when (not (contains? @downkeys k))
                                             (swap! pressedkeys assoc k ev)
                                             (swap! downkeys assoc k ev)))))
(.addEventListener js/window "keyup" (fn [ev]
                                       (let [k (event-key ev)]
                                         (swap! releasedkeys assoc k ev))))

;;;;; MOUSE

(def ^:private ^:no-doc mouse-position (atom [0 0]))

(defn mouse-global-pos
  "Gets the global mouse position in the browser. Not recommended for most use."
  []
  @mouse-position)

(defn mouse-pos
  "Gets the current mouse position in the form [x y]."
  [game]
  (let [rect (.getBoundingClientRect (:canvas game))
        [gx gy] @mouse-position]
    [(- gx (.-left rect)) (- gy (.-top rect))]))


(defn mouse-x
  "Gets the current mouse x coordinate."
  [game]
  ((mouse-pos game) 0))

(defn mouse-y
  "Gets the current mouse y coordinate."
  [game]
  ((mouse-pos game) 1))

;;;;; GLOBAL MOUSE SETUP

(.addEventListener js/window "mousemove" (fn [e]
                                           (reset!
                                            mouse-position
                                            [(.-clientX e) (.-clientY e)])))

(defn ^:no-doc init!
  "Initializes the input."
  [game]
  (aset (:canvas game) "onclick" (fn [ev] (enqueue-event! game :click ev))))

;;;;; GAME FUNCTIONS

(defn game
  "Creates a new game."
  ([width height canvas]
   (let [gl (gl/create-context canvas)
         g {:states (atom nil)
            :state (atom nil)
            :width width
            :height height
            :loop (atom true)
            :canvas canvas
            :event-queue (atom cljs.core.PersistentQueue.EMPTY)
            :handlers (atom nil)
            :handler-types (atom nil)
            :gl gl
            :audio-context (create-audio-context)
            :dt (atom 0)
            :now (atom (/ (.getTime (js/Date.)) 1000))}]
     (set! (.-width canvas) width)
     (set! (.-height canvas) height)
     (ezglib.gl/reset-viewport! gl)
     (init! g)
     (ezglib.gl/clear! gl)
     g))
  ([width height]
   (let [c (.createElement js/document "canvas")]
     (.appendChild (.-body js/document) c)
     (game width height c))))

(defn canvas
  "Gets the game canvas used for rendering."
  [game]
  (:canvas game))

(defn delta
  "Gets the change in time since the last frame."
  [game]
  @(:dt game))

(defn now
  "Gets the time of the current frame."
  [game]
  @(:now game))

(defn event-queue
  "Gets the event queue of the game."
  [game]
  @(:event-queue game))

(defn gl
  "Gets the gl context from the game."
  [game]
  (:gl game))

(defn audio-context
  "Gets the audio context from the game."
  [game]
  (:audio-context game))

(defn- ^:no-doc update-time
  "Updates the time in the game."
  [game]
  (let [new-now (/ (.getTime (js/Date.)) 1000)
        dt (- new-now @(:now game))]
    (reset! (:now game) new-now)
    (reset! (:dt game) dt)))

(defn- ^:no-doc game-loop!
  [game state-id callback-caller]
  (reset! (:state game) state-id)
  (reset! (:loop game) true)
  ((fn cb []
     (when @(:loop game) (callback-caller cb))
     (let [m (@(:states game) @(:state game))]
       (enqueue-keys! game)
       (update-time game)
       (if (:update m) ((:update m)))
       (if (:world m) (update! (:world m)))
       (if (:render m) ((:render m)))
       (handle-events! game)))))

(defn main-loop!
  "Runs the main loop of a game. If no fps
  is provided, will run at native fps."
  ([game fps]
   (game-loop! game @(:state game)
               (fn [cb]
                 (js/setTimeout cb (/ 1000 fps)))))
  ([game]
   (game-loop! game @(:state game) js/requestAnimationFrame)))

(defn end-game!
  "Ends the main loop of the game."
  [game]
  (reset! (:loop game) false))

;;;;; DEFAULT SYSTEMS

(let [origin (m/vec2 0 0)
      i2 (m/vec2 1 1)]
  (defn transform2d-system
    [game]
    "Creates a system that updates entities' local transformations
    by anylyzing their position, rotation, and scale."
    (system
     (matcher [] [] [:position :rotation :scale])
     (fn [e]
       (let [p (or (prop e :position) origin)
             r (or (prop e :rotation) 0)
             s (or (prop e :scale) i2)]
         (if (not (prop e :local-transform))
           (set-prop! e :local-transform (m/scalexy-rotatez-translatexy (.-x s) (.-y s) r (.-x p) (.-y p)))
           (m/scalexy-rotatez-translatexy (prop e :local-transform) (.-x s) (.-y s) r (.-x p) (.-y p))))))))

(defn render-system
  "Creates a simple render system that renders the :drawable property
  of entities."
  ([game & {:keys [shader camera color]}]
   (let [gl (:gl game)
         shader (or shader (texture-shader gl) )
         camera (or camera (m/m-ortho 0 (:width game) (:height game) 0 -1000000 1000000))
         color (or color (m/v 1 1 1 1))]
     (system
      (matcher [:drawable])
      (fn [e]
        (let [m (or (prop e :local-transform) m/m-identity4)
              d (prop e :drawable)]
          (gl/set-uniform! gl :modelViewMatrix m)
          (p/-draw! d)))
      (fn []
        (gl/clear! gl)
        (gl/use-shader! gl shader
                        :uniforms {:projectionMatrix camera
                                   :color color
                                   :tDiffuse 0}))))))

(let [origin (m/v 0 0 0)]
  (defn movement-system
    "Creates a system that moves an entity's :position based on its :velocity."
    [game]
    (system
     (matcher [:velocity :position])
     (fn [e]
       (if (not= (prop e :velocity) origin) (swap-prop! e :position m/add (m/mult (m/v3 (prop e :velocity)) (delta game))))
       nil))))

(defn rotate2d-system
  "Creates a system that changes an entity's :rotation based on its :angular-velocity."
  [game]
  (system
   (matcher [:angular-velocity :rotation])
   (fn [e]
     (if (not= (prop e :angular-velocity) 0) (swap-prop! e :rotation + (* (prop e :angular-velocity) (delta game))))
     nil)))
