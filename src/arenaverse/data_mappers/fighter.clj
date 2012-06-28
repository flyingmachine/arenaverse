(ns arenaverse.data-mappers.fighter
  (:require [arenaverse.data-mappers.db :as db]
            [arenaverse.lib.aws.s3 :as s3]
            [arenaverse.config :as config]
            [monger.collection]
            
            )

  (:import [org.bson.types ObjectId]
           [org.imgscalr Scalr Scalr$Method Scalr$Mode]
           [javax.imageio ImageIO]
           [java.awt.image BufferedImageOp]
           [java.awt.image BufferedImage]
           [org.apache.commons.io FilenameUtils]))

(db/add-db-fns "fighters")

(def *image-versions [["card" 192]
                      ["listing" 64]
                      ["battle" 432 638]])

(defn image-relative-path [version {:keys [_id image-extension]}]
  (str "fighters/" _id "/" version "." image-extension))

(defn image-path [version record]
  (str "/" (image-relative-path version record)))

(defn- normalize-image-extension [extension]
  (clojure.string/replace extension "jpeg" "jpg"))

(defn- image-fields [object-id image-extension]
  {:_id object-id
   :image-extension (normalize-image-extension image-extension)})

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

(defn- input->image-extension [input]
  (FilenameUtils/getExtension (:filename (:file input))))

;; TODO paul graham says this is crappy code - but is it easier to understand?
(defn input->images [input]
  (let [file-upload   (:file input)
        original-file (:tempfile file-upload)
        content-type  (:content-type file-upload)
        original-image {:version "original"
                        :file original-file
                        :content-type content-type}
        image-extension (normalize-image-extension (input->image-extension input))
        buff-img (ImageIO/read original-file)]
    
    ;; TODO make this a lazy seq
    (conj (map (fn [[version & dim] image-version]
                 {:version version
                  :file (buffered-image->input-stream
                         (apply resize (cons buff-img dim))
                         image-extension)
                  :content-type content-type
                  }) *image-versions)
          original-image)))

(defn bucket-name []
  (str "arenaverse-" (name config/env)))

(defn store-image [image, record]
  (s3/put-object config/*aws-credentials*
                 (bucket-name)
                 (image-relative-path (:version image) record)
                 (:file image)
                 {:content-type (:content-type image)}
                 #(.withCannedAcl % com.amazonaws.services.s3.model.CannedAccessControlList/PublicRead)))

(defn input->db-fields [input]
  (let [object-id (ObjectId.)]
    (merge
     (dissoc input :file)
     (image-fields object-id (input->image-extension input)))))

(defn create [input]
  (let [db-fields (input->db-fields input)]
    (db-insert db-fields)
    (future
      (map store-image (input->images input) (repeat db-fields)))))