(ns arenaverse.views.arenas
  (:require [arenaverse.views.common :as common]
            [noir.content.pages :as pages]
            [monger.collection :as mc])
  
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers
        arenaverse.views.routes)

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
    (map arena-details (mc/find-maps "arenas"))]))

(defpage-r edit {:keys [id]}
  (let [arena (mc/find-map-by-id "arenas" (ObjectId. id))]
    (common/layout
     [:h2 "Editing " (:name arena)]
     (form-to [:post (str "/arenas/" (:_id arena))]
            (arena-fields arena)
            [:p (submit-button "Update Arena")]))))

(defpage-r show {:keys [id]}
  (let [arena (mc/find-map-by-id "arenas" (ObjectId. id))]
    (common/layout
     [:h2 (:name arena)]
     [:p (:description arena)]
     [:p
      [:a {:href (url-for edit :id id)} "Edit"]])))

;; todo put name and description in separate map?
(defpage-r update {:keys [id name description]}
  (mc/update-by-id "arenas" (ObjectId. id) {:name name :description description})
  (common/layout
   [:h2 "Updated " name]
   [:p
    [:a {:href (url-for show :id id)} "Show"]]))

(defpage-r shiny {:as arena}
  (common/layout
   [:h2 "Create an Arena"]
   (form-to [:post "/arenas"]
            (arena-fields arena)
            [:p (submit-button "Create Arena")])))

(defpage-r create {:as arena}
  (insert "arenas" arena)
  (common/layout
   [:h2 "Arena Created!"]
   [:table (arena-details arena)]))