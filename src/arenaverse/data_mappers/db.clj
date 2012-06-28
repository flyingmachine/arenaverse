(ns arenaverse.data-mappers.db)

(defmacro add-db-fns [collection-name]
  (let [db-destroy 'db-destroy
        db-one 'db-one
        db-all 'db-all
        db-idstr 'db-idstr
        db-insert 'db-insert]
    `(let [collection-name# ~collection-name]
       (require 'monger.collection)

       (import 'org.bson.types.ObjectId)

       (defn ~db-destroy [_id#]
         (monger.collection/remove-by-id collection-name# (ObjectId. _id#)))

       (defn ~db-one [& [query-doc#]]
         (monger.collection/find-one-as-map collection-name# query-doc#))

       (defn ~db-all [& [query-doc#]]
         (monger.collection/find-maps collection-name# query-doc#))

       ;; TODO this one doesn't really feel like it belongs here
       (defn ~db-idstr [record#]
         (.toString (:_id record#)))

       (defn ~db-insert [fields#]
         (monger.collection/insert collection-name# fields#)))))