(ns arenaverse.models.battle
  (:require [arenaverse.config :as config]
            [monger.collection :as mc])

  (:use monger.operators)

  (:import [org.bson.types ObjectId]))

(def *collection "battles")

(defn destroy [_id]
  (mc/remove-by-id *collection (ObjectId. _id)))

(defn all []
  (mc/find-maps *collection))

(defn one []
  (mc/find-one-as-map *collection {}))

(defn record-winner [opponents winner]
  (if (some #(= winner %) opponents)
    (let [loser (some #(and (not= winner %) %) opponents)]
      (mc/update *collection

                 {(first opponents)  {$exists true}
                  (second opponents) {$exists true}}
                 
                 {$inc {winner 1 loser 0}}

                 :upsert true))))