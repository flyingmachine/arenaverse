(ns arenaverse.models.fighter
  (:require [arenaverse.config :as config]
            [monger.collection :as mc]
            [arenaverse.lib.aws.s3 :as s3])

  (:import [org.bson.types ObjectId]))

(defn image-relative-path [{:keys [_id image-extension]}]
  (str "fighters/" _id "/original." image-extension))

(defn image-path [record]
  (str "/" (image-relative-path record)))

(defn amazon-image-path [record]
  (str "https://s3.amazonaws.com/arenaverse-test" (image-path record)))

(defn- image-fields [object-id file]
  {:_id object-id
   :image-extension ((re-find #"\.([^.]*)$" (:filename file)) 1)})

(defn- save-image [object-id file]
  (if (not (= 0 (:size file)))
    (let [filename (image-relative-path (image-fields object-id file))]
      (println filename)
      (s3/put-object config/*aws-credentials*
                     "arenaverse-test"
                     filename
                     (:tempfile file)
                     {:content-type (:content-type file)}
                     #(.withCannedAcl % com.amazonaws.services.s3.model.CannedAccessControlList/PublicRead)))))

(defn create-fighter [attrs]
  (let [object-id (ObjectId.)
        file (:file attrs)
        fields (merge (dissoc attrs :file) (image-fields object-id file))]
    (mc/insert "fighters" fields)
    (save-image object-id file)
    fields))

(defn update-fighter [attrs]
  (let [mongo-id (ObjectId. (:_id attrs))
        record (mc/find-map-by-id "fighters" mongo-id)
        updated-fields (dissoc (merge record attrs) :_id :file)]
    (mc/update-by-id "fighters" mongo-id updated-fields)
    (save-image (:_id attrs) (:file attrs))
    updated-fields))

(defn all [& [query-doc]]
  (mc/find-maps "fighters" query-doc))
