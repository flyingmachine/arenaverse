(ns arenaverse.models.fighter
  (:require [arenaverse.config :as config]
            [monger.collection :as mc]
            [aws.sdk.s3 :as s3])

  (:import [org.bson.types ObjectId]))

(defn image-relative-path [id extension]
  (str "fighters/" id "/original." extension))

(defn image-path [id extension]
  (str (image-relative-path id extension)))

(defn create-fighter [attrs]
  (let [object-id (ObjectId.)
        file (:file attrs)
        image-extension ((re-find #"\.([^.]*)$" (:filename file)) 1)
        fields (assoc (dissoc attrs :file) :_id object-id :image-extension image-extension)]
    (println "first")
    (mc/insert "fighters" fields)
    (let [filename (image-relative-path object-id image-extension)]
      (if (not (= "0" (:size file)))
        (s3/put-object config/*aws-credentials*
                       "arenaverse-test"
                       filename
                       (:tempfile file)
                       {:content-type (:content-type file)})))))

(defn all [])