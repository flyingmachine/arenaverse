(ns arenaverse.data-mappers.arena
  (:require [arenaverse.data-mappers.db :as db]
            [cemerick.friend :as friend]))

(db/add-db-reqs)
(let [collection-name "arenas"]
  (db/add-db-fns collection-name)
  (db/add-finder-fns))

(defn url-friendly [name]
  (clojure.string/replace name #"[\W]" "-"))

(defn- create-input->db-fields [input]
  (let [object-id (ObjectId.)]
    (merge input
           {:_id object-id
            :user-id (:_id (friend/current-authentication))
            :url-id (str (url-friendly name) " " (.substring (.toString object-id) 20))})))

(defn create [input]
  (db-insert (create-input->db-fields input)))

(defn destroy [_id]
  (db-destroy (ObjectId. _id)))

(defn update [_id, input]
  (db-update-by-id (ObjectId. _id) {$set input}))

(defn by-user [user-id]
  (all {:user-id user-id}))
