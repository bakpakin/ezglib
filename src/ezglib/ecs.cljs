(ns ezglib.ecs
  (:require [ezglib.event :as event]
            [ezglib.util :as util]))

;;;;; STATIC STORAGE ;;;;;

(def ^:no-doc _entities (atom {}))
(def ^:no-doc _systems (atom {}))

;;;;; DEFTYPE ;;;;;

(deftype ^:private ^:no-doc World [game root ^:mutable systems ^:mutable entities]
  Object
  (toString [_] (str "systems: " (select-keys systems @_systems) ", entities: " (select-keys entities @_entities))))
(deftype ^:private ^:no-doc Entity [id ^:mutable world ^:mutable properties ^:mutable children]
  Object
  (toString [_] (str "id: " id ", properties: "properties))
  IEquiv
  (-equiv [_ o] (= id (.-id o))))
(deftype ^:private ^:no-doc System [id ^:mutable world matcher func one-time-func ^:mutable entities]
  Object
  (toString [_] (str "id: " id ", entities: " entities))
  IEquiv
  (-equiv [_ o] (= id (.-id o))))

(let [next-id (atom 0)]
  (defn- ^:no-doc uid [] (swap! next-id inc)))

;;;;; ENTITY AND SYSTEM ;;;;;

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

(defn- ^:no-doc try-add!
  [system-id entity-id]
  (let [s (get @_systems system-id)
        m (.-matcher s)
        e (get @_entities entity-id)]
    (if (m e)
      (set! (.-entities s) (conj (.-entities s) entity-id)))))

(defn entity
  "Creates a new entity."
  [properties & children]
  (let [id (uid)
        e (Entity. id nil properties (set (map #(.-id %) children)))]
    (swap! _entities assoc id e)
    e))

(defn prop
  "Get a property of an entity."
  [entity property]
  (get (.-properties entity) property))

(defn set-prop!
  "Sets a property of an entity."
  [entity property value]
  (if (get (.-properties entity) property)
    (set! (.-properties entity) (assoc (.-properties entity) property value))
    (do
      (set! (.-properties entity) (assoc (.-properties entity) property value))
      (doseq [s-id (.-systems (.-world entity))]
        (try-add! s-id (.-id entity))))))

(defn swap-prop!
  "Similar to swap! on atoms."
  [entity property f & args]
  (let [prev (prop entity property)
        new (apply f prev args)
        props (.-properties entity)]
    (set-prop! entity property new)
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
  ([matcher func once-per-frame-func]
   (let [id (uid)
         s (System. id nil matcher func once-per-frame-func #{})]
     (swap! _systems assoc id s)
     s))
  ([matcher func]
   (system matcher func (fn [] nil))))

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
      (swap! _entities dissoc (.-id i))
      (swap! _systems dissoc (.-id i))))
  nil)

;;;;; WORLD ;;;;;

(defn world
  "Creates a new world."
  [game & items]
  (let [r (entity {::root true})
        r-id (.-id r)
        w (World. game r #{} #{r-id})]
    (set! (.-world r) w)
    (apply add! w items)))

(defn update!
  "Updates a world."
  [world]
  (doseq [s-id (.-systems world)]
    (let [s (get @_systems s-id)]
      ((.-one-time-func s))
      (doseq [e-id (.-entities s)]
        (let [e (get @_entities e-id)]
          ((.-func s) e)))
      (event/handle-events! (.-game world)))))
