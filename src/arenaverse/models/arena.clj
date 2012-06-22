(ns arenaverse.models.arena
  (:require [arenaverse.config :as config]
            [monger.collection :as mc])

  (:import [org.bson.types ObjectId]))

(def *collection "arenas")

(defn destroy [_id]
  (mc/remove-by-id *collection (ObjectId. _id)))