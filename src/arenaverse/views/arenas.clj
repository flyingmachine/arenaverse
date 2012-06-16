(ns arenaverse.views.arenas
  (:require [arenaverse.views.common :as common]
            [noir.content.pages :as pages])
  
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers
        arenaverse.views.routes
        [monger.collection :only [insert insert-batch find-maps find-map-by-id]])

  (:import [org.bson.types ObjectId]))


(defpartial arena-fields [{:keys [name description]}]
  [:table
   [:tr
    [:td (label "name" "Name")]
    [:td (text-field "name" name)]]
   [:tr
    [:td (label "description" "Description")]
    [:td (text-field "description" description)]]])

(defpartial arena-details [{:keys [name description _id]}]
  [:tr
   [:td [:a {:href (url-for-r :arenas/show {:id _id})} name]]
   [:td description]])

(defpage-r listing []
  (common/layout
   [:h2 "Arenas"]
   [:table
    (map arena-details (find-maps "arenas"))]))

(defpage-r show {:keys [id]}
  (let [arena (find-map-by-id "arenas" (ObjectId. id))]
    (common/layout
     [:h2 "Arena: " (:name arena)]
     [:table
      (arena-details arena)])))

(defpage-r shiny {:as arena}
  (common/layout
   [:h2 "Create an Arena"]
   (form-to [:post "/arenas"]
            (arena-fields arena)
            (submit-button "Create Arena"))))

(defpage-r create {:as arena}
  (insert "arenas" arena)
  (common/layout
   [:h2 "Arena Created!"]
   [:table (arena-details arena)]))