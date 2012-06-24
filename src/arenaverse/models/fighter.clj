(ns arenaverse.models.fighter
  (:require [arenaverse.config :as config]
            [monger.collection :as mc]
            [arenaverse.lib.aws.s3 :as s3])

  (:import [org.bson.types ObjectId]
           [org.imgscalr Scalr Scalr$Method Scalr$Mode]
           [javax.imageio ImageIO]
           [java.awt.image BufferedImageOp]
           [java.awt.image BufferedImage]
           [org.apache.commons.io FilenameUtils]))

(def *image-versions [["card" 192]
                      ["listing" 64]
                      ["battle" 432 638]])

(def *collection "fighters")

(defn image-relative-path [version {:keys [_id image-extension]}]
  (str "fighters/" _id "/" version "." image-extension))

(defn image-path [version record]
  (str "/" (image-relative-path version record)))

(defn bucket-name []
  (str "arenaverse-" (name config/env)))

(defn amazon-image-path [version record]
  (str "https://s3.amazonaws.com/" (bucket-name) (image-path version record)))

(defn- image-fields [object-id image-extension]
  {:_id object-id
   :image-extension (clojure.string/replace image-extension "jpeg" "jpg")})

(defn- save-image [path file content-type]
  (s3/put-object config/*aws-credentials*
                 (bucket-name)
                 path
                 file
                 {:content-type content-type}
                 #(.withCannedAcl % com.amazonaws.services.s3.model.CannedAccessControlList/PublicRead)))

(defn- resize
  ([image box]
     (resize image box box))
  ([image target-width target-height]
     (let [width  (.getWidth image)
           height (.getHeight image)
           fit (if (> (/ target-width width) (/ target-height height)) Scalr$Mode/FIT_TO_HEIGHT Scalr$Mode/FIT_TO_WIDTH)]
       (Scalr/resize image Scalr$Method/ULTRA_QUALITY fit target-width target-height (into-array BufferedImageOp [])))))

(defn- buffered-image->input-stream [buffered-image extension]
  (let [os (java.io.ByteArrayOutputStream.)]
    (ImageIO/write buffered-image extension os)
    (java.io.ByteArrayInputStream. (.toByteArray os))))

(defn- resize-and-save-image [object-id file-upload]
  (if (not (= 0 (:size file-upload)))
    (let [extension    (FilenameUtils/getExtension (:filename file-upload))
          content-type (:content-type file-upload)
          file         (:tempfile file-upload)
          img-fields   (image-fields object-id extension)]
      ;; save original
      (save-image (image-relative-path "original" img-fields) file content-type)
      (let [buff-img (ImageIO/read file)]
        (doseq [[version & dim] *image-versions]
          (save-image (image-relative-path version img-fields)
                      (buffered-image->input-stream (apply resize (cons buff-img dim)) extension)
                      version))))))

(defn delete-images [record]
  (doseq [[vname] (conj *image-versions ["original"])]
    (s3/delete-object config/*aws-credentials* "arenaverse-test" (image-relative-path vname record))))

;; (Scalr/resize (ImageIO/read (java.io.File. "/Users/daniel/Desktop/dachshunds.jpg")) (int 150))

(defn create [attrs]
  (let [object-id (ObjectId.)
        file-upload (:file attrs)
        fields (merge
                (dissoc attrs :file)
                (image-fields object-id (FilenameUtils/getExtension (:filename file-upload))))]
    (mc/insert *collection fields)
    (future (resize-and-save-image object-id file-upload))
    fields))

(defn image-extension-for-update [attrs record]
  (let [file-upload (:file attrs)]
    (if (not= 0 (:size file-upload))
      (image-fields "" (FilenameUtils/getExtension (:filename file-upload)))
      (select-keys record [:image-extension]))))

(defn update [attrs]
  (let [_id (:_id attrs)
        bson-id (ObjectId. _id)
        record (mc/find-map-by-id *collection bson-id)
        updated-fields (dissoc (merge record
                                      attrs
                                      (image-extension-for-update attrs record))
                               :_id
                               :file)]
    (mc/update-by-id *collection bson-id updated-fields)
    (future (resize-and-save-image _id (:file attrs)))
    updated-fields))

(defn destroy [_id]
  (delete-images (mc/find-map-by-id *collection (ObjectId. _id)))
  (mc/remove-by-id *collection (ObjectId. _id)))

(defn one [& [query-doc]]
  (mc/find-one-as-map *collection query-doc))

(defn all [& [query-doc]]
  (mc/find-maps *collection query-doc))

(defn idstr [record]
  (.toString (:_id record)))