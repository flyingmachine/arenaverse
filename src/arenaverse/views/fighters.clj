(ns arenaverse.views.fighters
  (:require [arenaverse.views.common :as common]
            [arenaverse.config :as config]
            [noir.content.pages :as pages]
            [noir.response :as resp]
            [noir.session :as session]
            [noir.util.s3 :as s3]
            [monger.collection :as mc])
  
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers
        arenaverse.views.routes)

  (:import [org.bson.types ObjectId]))

(defpage-r create {:keys [arena-id name bio file]}
  (if (not (= "0" (:size file)))
    (do
      (binding [s3/*s3* (s3/service config/*aws-credentials*)]
        (s3/put! "arenaverse-test" (:tempfile file)))
      (resp/redirect (url-for-r :arenas/show :id arena-id)))))