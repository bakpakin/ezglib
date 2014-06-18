(ns ezglib.mode
  (:require [ezglib.event :as event]))

(defn add-mode!
  "Adds a mode to the game. A mode is a
  function that takes one parameter, game, and updates
  the game. The mode function is called once every time
  through the main loop."
  [game mode-id mode]
  (swap! (:modes game) assoc mode-id mode))

(defn remove-mode!
  "Removes a mode from the game."
  [game mode-id]
  (swap! (:modes game) dissoc mode-id))

(defn current-mode-id
  "Gets the id of the current mode of the game."
  [game]
  @(:mode game))

(defn current-mode
  "Gets the current mode of the game."
  [game]
  (@(:modes game) @(:mode game)))

(defn set-mode!
  "Sets the mode of the game. If mode-id
  does not exist in the game, does nothing and returns nil. Else
  sets the game mode and returns the new mode."
  [game mode-id]
  (if-let [new-mode (@(:modes game) mode-id)]
    (let []
      (reset! (:mode game) mode-id)
      new-mode)
    nil))

(defn modes
  "Gets all availble modes of the game."
  [game]
  @(:modes game))

(defn mode
  "Makes a bare-bones game mode."
  [update-fn]
  {:update (fn [gm]
             (update-fn gm)
             (event/drain! gm))})
