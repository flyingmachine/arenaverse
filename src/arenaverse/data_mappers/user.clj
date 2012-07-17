(ns arenaverse.data-mappers.user
  (:require [arenaverse.data-mappers.db :as db]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds]
                             [openid :as openid])))

(db/add-db-reqs)
(let [collection-name "users"]
  (db/add-db-fns collection-name)
  (db/add-finder-fns))


(defn- create-input->db-fields [input]
  (merge input
         {:_id (ObjectId.)
          :password (creds/hash-bcrypt (:password input))
          :roles ["user"]}))

;; TODO use threading macro
(defn create [input]
  (db-insert (create-input->db-fields input)))

(defn destroy [_id]
  (db-destroy (ObjectId. _id)))

;; handle password updating
(defn update [_id, input]
  (db-update-by-id (ObjectId. _id) input))