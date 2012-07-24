(ns arenaverse.lib.dev
  (:require
   [arenaverse.data-mappers.fighter :as fighter]
   [arenaverse.data-mappers.arena :as arena]
   [arenaverse.data-mappers.user :as user]
   [monger.collection :as mc])

  (:import [org.bson.types ObjectId]))

(defn mongo-connect! []
  (monger.core/connect-via-uri! "mongodb://127.0.0.1/omgsmackdown-dev"))

(defn seed! []
  (user/create {:username "daniel" :password "test123."})
  (let [u-id (:_id (user/one))]
    (arena/db-insert {:_id (ObjectId.) :name "Republicans vs. Monsters" :fight-text "Which creature is scarier?" :user-id u-id}))
  (let [a-id (:_id (arena/one))]
    (fighter/create {:name "John McCain" :arena-id a-id})
    (fighter/create {:name "Vampire", :arena-id a-id})))

(defn clear-data! []
  (mc/remove "users")
  (mc/remove "fighters")
  (mc/remove "arenas"))


(defn re-seed! []
  (clear-data!)
  (seed!))
