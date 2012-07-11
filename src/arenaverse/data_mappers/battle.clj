(ns arenaverse.data-mappers.battle
  (:require [arenaverse.data-mappers.db :as db]
            [monger.collection :as mc])
  
  (:use monger.operators))

(db/add-db-reqs)
(let [collection-name "battles"]
  (db/add-db-fns collection-name)
  (db/add-finder-fns))

(defn destroy [_id]
  (db-destroy (ObjectId. _id)))

(defn record-for-pair [_ids]
  (dissoc (one {(first _ids)  {$exists true}
                (second _ids) {$exists true}})
          :_id))

(defn record-winner! [opponents winner]
  (if (some #(= winner %) opponents)
    (let [loser (some #(and (not= winner %) %) opponents)]
      (db-update
       {(first opponents)  {$exists true}
        (second opponents) {$exists true}}
       {$inc {winner 1 loser 0}}       
       :upsert true))))