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

(defn create-fighter [attrs]
  (let [object-id (ObjectId.)
        file (:file attrs)
        image-extension ((re-find #"\.([^.]*)$" (:filename file)) 1)
        image-fields {:_id object-id :image-extension image-extension}
        fields (merge (dissoc attrs :file) image-fields)]
    (mc/insert "fighters" fields)
    (let [filename (image-relative-path image-fields)]
      (if (not (= "0" (:size file)))
        (s3/put-object config/*aws-credentials*
                       "arenaverse-test"
                       filename
                       (:tempfile file)
                       {:content-type (:content-type file)}
                       #(.withCannedAcl % com.amazonaws.services.s3.model.CannedAccessControlList/PublicRead))))))

(defn all []
  (mc/find-maps "fighters"))