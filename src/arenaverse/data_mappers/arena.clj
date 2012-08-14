(ns arenaverse.data-mappers.arena
  (:require [arenaverse.data-mappers.db :as db]
            [cemerick.friend :as friend]))

(db/add-db-reqs)
(let [collection-name "arenas"]
  (db/add-db-fns collection-name)
  (db/add-finder-fns))

(defn url-friendly [name]
  (clojure.string/replace name #"[\W]" "-"))

(defn shortname [name object-id]
  (clojure.string/lower-case (str (url-friendly name) "-" (.substring (.toString object-id) 20))))

(defn- create-input->db-fields [input]
  (let [object-id (ObjectId.)]
    (merge input
           {:_id object-id
            :user-id (:_id (friend/current-authentication))
            :shortname (shortname (:name input) object-id)})))

(defn create [input]
  (let [db-fields (create-input->db-fields input)]
    (db-insert db-fields)
    db-fields))

(defn destroy [shortname]
  (let [object-id (:_id (db-one {:shortname shortname}))]
    (db-destroy object-id)))

(defn update [_id, input]
  (db-update-by-id (ObjectId. _id) {$set (dissoc input :user-id :_id)}))

(defn by-user [user-id]
  (all {:user-id user-id}))
