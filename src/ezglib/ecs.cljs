(ns ezglib.ecs
  (:require [ezglib.event :as event]))

;;;;; DEFTYPE ;;;;;

(deftype ^:private ^:no-doc World [game root ^:mutable systems ^:mutable entities]
  Object
  (toString [_] (str "systems: " systems ", entities: " entities)))
(deftype ^:private ^:no-doc Entity [id ^:mutable world ^:mutable properties ^:mutable children]
  Object
  (toString [_] (str "id: " id ", properties: "properties))
  IEquiv
  (-equiv [_ o] (= id (.-id o))))
(deftype ^:private ^:no-doc System [id ^:mutable world matcher func ^:mutable entities]
  Object
  (toString [_] (str "id: " id ", update-fn: "func))
  IEquiv
  (-equiv [_ o] (= id (.-id o))))

(let [next-id (atom 0)]
  (defn- ^:no-doc uid [] (swap! next-id inc)))

;;;;; STATIC STORAGE ;;;;;

(def ^:private ^:no-doc entities (atom {}))
(def ^:private ^:no-doc systems (atom {}))

;;;;; FUNCTIONS ;;;;;

(defn entity?
  "Is x an entity?"
  [x]
  (= Entity (type x)))

(defn world?
  "Is x a world?"
  [x]
  (= World (type x)))

(defn system?
  "Is x a system?"
  [x]
  (= System (type x)))

(defn entity
  "Creates a new entity."
  [properties & children]
  (let [id (uid)
        e (Entity. id nil properties (set (map #(.-id %) children)))]
    (swap! entities assoc id e)
    e))

(defn prop
  "Get a property of an entity."
  [entity property]
  (get (.-properties entity) property))

(defn set-prop!
  "Sets a property of an entity."
  [entity property value]
  (set! (.-property entity) value))

(defn swap-prop!
  "Similar to swap! on atoms."
  [entity property f & args]
  (let [prev (prop entity property)
        new (apply f prev args)
        props (.-properties entity)]
    (set! (.-properties entity) (assoc props property new))
    new))

(defn matcher
  "Creates a new system matcher. A system matcher matches entities to systems based
  on their properties."
  ([required excluded one-required]
   (fn [e]
     (let [f #(get (.-properties e) %)]
       (and
        (every? f required)
        (not-any? f excluded)
        (some f one-required)))))
  ([required excluded]
   (fn [e]
     (let [f #(get (.-properties e) %)]
       (and
        (every? f required)
        (not-any? f excluded)))))
  ([required]
   (fn [e]
     (let [f #(get (.-properties e) %)]
       (every? f required)))))

(defn system
  "Creates a new system. Systems process entities, which hold game data."
  [matcher func]
  (let [id (uid)
        s (System. id nil matcher func [])]
    (swap! systems assoc id s)
    s))

(defn- ^:no-doc try-add!
  [system-id entity-id]
  (let [s (get @systems system-id)
        m (.-matcher s)
        e (get @entities entity-id)]
    (if (m e)
      (set! (.-entities s) (conj (.-entities s) entity-id)))))

(defn add-system!
  "Adds a system to the world. Returns the world."
  [world system]
  (set! (.-world system) world)
  (set! (.-systems world) (conj (.-systems world) (.-id system)))
  (doseq [e-id (.-entities world)]
    (try-add! (.-id system) e-id))
  world)

(defn- ^:no-doc add-child!
  [world entity]
  (set! (.-world entity) world)
  (set! (.-entities world) (conj (.-entities world) (.-id entity)))
  (doseq [s-id (.-systems world)]
    (try-add! s-id (.-id entity)))
  (doseq [c (.-children entity)]
    (add-child! world c)))

(defn add-entity!
  "Adds an entity to the world and recursively adds children. Returns the world."
  [world entity]
  (add-child! world entity)
  (set! (.-children (.-root world)) (conj (.-children (.-root world)) entity))
  world)

(defn add!
  "Adds systems and entities to a world. Returns the world."
  [world & items]
  (let [ents (filter entity? items)
        systs (filter system? items)]
    (reduce add-entity! (reduce add-system! world systs) ents)))

(defn remove!
  "Removes systems and entities from their worlds. If removing
  an entity, children are recursively removed."
  [& items]
  (doseq [i items]
    (if (system? i)
      (do
        (set! (.-systems (.-world i)) (disj (.-systems (.-world i)) (.-id i))))
      (when (entity? i)
        (set! (.-entities (.-world i)) (disj (.-entities (.-world i)) (.-id i)))
        (apply remove! (.-children i)))))
  nil)

(defn destroy!
  "Destroys entities and systems, removing them from their worlds.
  Children of entities are recursively destroyed."
  [& items]
  (apply remove! items)
  (doseq [i items]
    (if (entity? i)
      (swap! entities dissoc (.-id i))
      (swap! systems dissoc (.-id i))))
  nil)

(defn world
  "Creates a new world."
  [game & items]
  (let [r (entity {::root true})
        r-id (.-id r)]
    (apply add! (World. game r #{} #{r-id}) items)))

(defn update
  "Updates a world."
  [world]
  (doseq [s-id (.-systems world)]
    (let [s (get @systems s-id)]
      (doseq [e-id (.-entities s)]
        (let [e (get @entities e-id)]
          (swap! entities assoc e-id ((.-func s) e))))
      (event/handle-events! (.-game world)))))
