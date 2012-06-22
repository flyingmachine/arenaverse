(ns arenaverse.views.battles
  (:require [arenaverse.views.common :as common]
            [arenaverse.views.admin.fighters :as fighters]
            [arenaverse.models.fighter :as fighter]
            [noir.session :as session]
            [monger.collection :as mc])
  
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        arenaverse.views.routes)
  
  (:import [org.bson.types ObjectId]))

(defpage-r listing []
  (common/layout
   [:h1 "Infinite Battles!"]
   [:table]))
