(ns arenaverse.data-mappers.db)

;; TODO ~' Insanity! #cthulhu
;; These macros are meant to infect the namespace with functions. Why
;; would I want to do this? Should I take heed of the fact that
;; Clojure really doesn't want me to?

;; I wrote these fucntions to avoid having to write collection-name
;; all over the place

(defmacro add-db-reqs []
  '(do
     (require 'monger.collection)
     (import 'org.bson.types.ObjectId)))

(defmacro add-db-fns [collection-name]
  `(let [collection-name# ~collection-name]
    (def ~'db-destroy (partial monger.collection/remove-by-id collection-name#))     
    (def ~'db-one (partial monger.collection/find-one-as-map collection-name#))
    (def ~'db-one-by-id (partial monger.collection/find-map-by-id collection-name#))
    (def ~'db-all (partial monger.collection/find-maps collection-name#))
    (def ~'db-insert (partial monger.collection/insert collection-name#))
    (def ~'db-update-by-id (partial monger.collection/update-by-id collection-name#))
    (def ~'db-update (partial monger.collection/update collection-name#))))

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
       (if-let [r (db-one query-doc)]
         (object-id->idstr r)))
     
     (defn one-by-id [_id]
       (if-let [r (db-one-by-id (ObjectId. _id))]
         (object-id->idstr r)))))