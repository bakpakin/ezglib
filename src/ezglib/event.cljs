(ns ezglib.event)

;The queue that holds events.
(declare event-queue)

(defn enqueue-event!
  "Queues an event that happened in the game."
  [event-type & params]
  (swap! event-queue conj [event-type (vec params)]))

(defn event-mode
  "Makes a game mode that gets events from
  the game and executes registered handlers."
  [update-fn]
    (let [handlers (atom {})
          f (fn [gm]
              (update-fn gm)
              (enqueue-event! ::end-update)
              (while (when-let [n (peek @event-queue)] (not (= ::end-update n)))
                (let [[et params] (peek @event-queue)]
                  (when-let [hs (@handlers et)]
                    (doseq [h (vals hs)]
                      (apply h params)))
                  (swap! event-queue pop))))]
      {:update f
       :handlers handlers
       :handler-types (atom {})
       :next-id (atom 0)}))

(defn drain!
  "Drains all events in the event queue."
  []
  (reset! event-queue cljs.core.PersistentQueue.EMPTY))

(defn add-handler!
  "Adds a handler to the event-mode for a certain event type.
  Returns the handler id."
  [event-mode event-type handler]
  (let [id (swap! (:next-id event-mode) inc)]
    (swap! (:handlers event-mode) assoc-in [event-type id] handler)
    (swap! (:handler-types event-mode) assoc id event-type)
    id))

(defn handler
  "Gets the handler associated with the given id
  in the event-mode."
  [event-mode id]
  (if-let [et (@(:handler-types event-mode) id)]
    (get-in @(:handlers event-mode) [et id])))

(defn remove-handler!
  "Removes a handler from the event-mode by id.
  Returns the handler."
  [event-mode id]
  (if-let [et (@(:handler-types event-mode) id)]
    (let [h (get-in @(:handlers event-mode) [et id])]
      (swap! (:handler-types event-mode) dissoc id)
      (swap! (:handlers event-mode)
         (fn [ets e-t i] (assoc ets e-t (dissoc (ets e-t) i)))
         et
         id)
      h)))

(defn init!
  "Initializes events."
  [canvas]
  (def ^:private event-queue (atom cljs.core.PersistentQueue.EMPTY))
  (set! (.-onclick canvas) (fn [ev] (enqueue-event! :click ev)))
  (set! (.-onkeypress js/document) (fn [ev] (enqueue-event! :key ev))))
