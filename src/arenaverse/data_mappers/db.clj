(ns arenaverse.data-mappers.db)

(defmacro add-db-fns [collection-name]
  ;; This repetitive mapping is necessary to avoid including the
  ;; entire namespace in the function name, the result of syntax quoting
  (let [db-destroy 'db-destroy
        db-one 'db-one
        db-one-by-id 'db-one-by-id
        db-all 'db-all
        db-idstr 'db-idstr
        db-insert 'db-insert
        db-update 'db-update]
    `(let [collection-name# ~collection-name]
       (require 'monger.collection)

       (import 'org.bson.types.ObjectId)

       (defn ~db-destroy [_id#]
         (monger.collection/remove-by-id collection-name# _id#))

       (defn ~db-one [& [query-doc#]]
         (monger.collection/find-one-as-map collection-name# query-doc#))

       (defn ~db-one-by-id [_id#]
         (monger.collection/find-map-by-id collection-name# _id#))

       (defn ~db-all [& [query-doc#]]
         (monger.collection/find-maps collection-name# query-doc#))

       ;; TODO this one doesn't really feel like it belongs here
       ;; because it doesn't have to do with persistence
       (defn ~db-idstr [_id#]
         (.toString _id#))

       (defn ~db-insert [fields#]
         (monger.collection/insert collection-name# fields#))

       (defn ~db-update [_id#, fields#]
         (monger.collection/update-by-id collection-name# _id# fields#)))))