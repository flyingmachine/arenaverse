(ns arenaverse.models.arena
  (:require [arenaverse.config :as config]
            [monger.collection :as mc])

  (:import [org.bson.types ObjectId]))

(def *collection "arenas")

(defn destroy [_id]
  (mc/remove-by-id *collection (ObjectId. _id)))

(defn all []
  (mc/find-maps *collection))

(defn one []
  (mc/find-one-as-map *collection {}))

(defn idstr [record]
  (.toString (:_id record)))