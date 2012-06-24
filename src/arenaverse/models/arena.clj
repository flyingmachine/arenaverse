(ns arenaverse.models.arena
  (:require [arenaverse.config :as config]
            [monger.collection :as mc])

  (:import [org.bson.types ObjectId]))

(def *collection "arenas")

(defn destroy [_id]
  (mc/remove-by-id *collection (ObjectId. _id)))

(defn all []
  (mc/find-maps *collection))

(defn one [& [query-doc]]
  (mc/find-one-as-map *collection query-doc))

(defn idstr [record]
  (.toString (:_id record)))