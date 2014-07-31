(ns ezglib.event)

(defn enqueue-event!
  "Queues an event that happened in the game."
  [game event-type & params]
  (if (seq (get @(:handlers game) event-type))
    (swap! (:event-queue game) conj [event-type (vec params)])))

(defn ^:no-doc handle-events!
  "Executes all event handlers in the game-mode for the currently queued events.
  Users of this library should not usually have to call this."
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

(defn add-handler!
  "Adds a handler to the mode for a certain event type.
  Returns the handler id."
  [mode event-type handler]
  (let [id (swap! (:next-id mode) inc)]
    (swap! (:handlers mode) assoc-in [event-type id] handler)
    (swap! (:handler-types mode) assoc id event-type)
    id))

(defn handler
  "Gets the handler associated with the given id
  in the mode."
  [mode id]
  (if-let [et (@(:handler-types mode) id)]
    (get-in @(:handlers mode) [et id])))

(defn remove-handler!
  "Removes a handler from the mode by id.
  Returns the handler."
  [mode id]
  (if-let [et (@(:handler-types mode) id)]
    (let [h (get-in @(:handlers mode) [et id])]
      (swap! (:handler-types mode) dissoc id)
      (swap! (:handlers mode)
         (fn [ets e-t i] (assoc ets e-t (dissoc (ets e-t) i)))
         et
         id)
      h)))
