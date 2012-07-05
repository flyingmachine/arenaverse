(ns arenaverse.data-mappers.db)

;; TODO ~' Insanity! #cthulhu
;; These macros are meant to infect the namespace with functions. Why
;; would I want to do this? Should I take heed of the fact that
;; Clojure really doesn't want me to?

;; I wrote these fucntions to avoid having to write collection-name
;; all over the place
(defmacro add-db-fns [collection-name]
  `(let [collection-name# ~collection-name]
     (require 'monger.collection)
     
     (import 'org.bson.types.ObjectId)

     (defn ~'db-destroy [_id#]
       (monger.collection/remove-by-id collection-name# _id#))

     (defn ~'db-one [& [query-doc#]]
       (monger.collection/find-one-as-map collection-name# query-doc#))

     (defn ~'db-one-by-id [_id#]
       (monger.collection/find-map-by-id collection-name# _id#))

     (defn ~'db-all [& [query-doc#]]
       (monger.collection/find-maps collection-name# query-doc#))

     (defn ~'db-insert [fields#]
       (monger.collection/insert collection-name# fields#))

     (defn ~'db-update [_id#, fields#]
       (monger.collection/update-by-id collection-name# _id# fields#))))

;; TODO I don't like mapping in the all fn, feels wasteful.

;; These methods are meant to generate the representations which
;; non-db parts of the code will use. They all convert ObjectId's to
;; strings because no other part of the system should care about ObjectId's
(defmacro add-finder-fns []
  '(do
     
     ;; TODO this doesn't feel like it belongs here. It's a helper
     ;; method. But this macro approach is infecting everything!
     (defn idstr [record]
       (.toString (:_id record)))
     
     (defn object-id->idstr [record]
       (assoc record :_id (idstr record)))

     (defn all [& [query-doc]]
       (map object-id->idstr (db-all query-doc)))
     
     (defn one [& [query-doc]]
       (object-id->idstr (db-one query-doc)))
     
     (defn one-by-id [_id]
       (object-id->idstr (db-one-by-id (ObjectId. _id))))))