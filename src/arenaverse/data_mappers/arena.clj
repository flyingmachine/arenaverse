(ns arenaverse.data-mappers.arena
  (:require [arenaverse.data-mappers.db :as db]))

(db/add-db-reqs)
(let [collection-name "arenas"]
  (db/add-db-fns collection-name)
  (db/add-finder-fns))

(defn destroy [_id]
  (db-destroy (ObjectId. _id)))