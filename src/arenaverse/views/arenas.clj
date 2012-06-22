(ns arenaverse.views.arenas
  (:require [arenaverse.views.common :as common]
            [arenaverse.views.fighters :as fighters]
            [arenaverse.models.fighter :as fighter]
            [noir.session :as session]
            [monger.collection :as mc])
  
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        arenaverse.views.routes)

  (:import [org.bson.types ObjectId]))

(declare show)

(defpartial arena-fields [{:keys [name fight-text]}]
  [:table
   [:tr
    [:td (label "name" "Name")]
    [:td (text-field "name" name)]]
   [:tr
    [:td (label "fight-text" "Fight Text")]
    [:td (text-field "fight-text" fight-text)]]])

(defpartial arena-details [{:keys [name fight-text _id]}]
  [:tr
   [:td [:a {:href (url-for-r :arenas/show {:_id _id})} name]]
   [:td fight-text]])

(defpage-r listing []
  (common/layout
   [:h1 "Arenas"]
   [:table
    (map arena-details (mc/find-maps "arenas"))]))

(defpage-r shiny {:as arena}
  (common/layout
   [:h1 "Create an Arena"]
   (form-to [:post "/arenas"]
            (arena-fields arena)
            [:p (submit-button "Create Arena")])))

(defpage-r edit {:keys [_id]}
  (let [arena (mc/find-map-by-id "arenas" (ObjectId. _id))]
    (common/layout
     [:h1 "Editing Arena: " (:name arena)]
     (form-to [:post (str "/arenas/" (:_id arena))]
            (arena-fields arena)
            [:p (submit-button "Update Arena")]))))

(defpage-r show {:keys [_id]}
  (let [arena (mc/find-map-by-id "arenas" (ObjectId. _id))]
    (common/layout
     [:h1 (:name arena)]
     (if-let [msg (session/flash-get)]
       [:p.info msg])
     [:p [:a {:href (url-for-r :arenas/edit arena)} "Edit"]]
     [:p (:fight-text arena)]

     [:div#new-fighter
      [:h2 "New Fighter"]
      (form-to {:enctype "multipart/form-data"}
               [:post (url-for-r :fighters/create)]
               (hidden-field :arena-id _id)
               [:table
                (fighters/fighter-fields {})
                [:tr
                 [:td]
                 [:td (submit-button "Create Fighter")]]])]
     
     [:div#fighters
      [:h2 "Fighters"]
      (fighters/thumbs {:arena-id _id})])))

;; todo put name and fight-text in separate map?
(defpage-r update {:keys [_id name fight-text]}
  (mc/update-by-id "arenas" (ObjectId. _id) {:name name :fight-text fight-text})
  (session/flash-put! "Arena updated!")
  (arenas-show {:_id _id}))

(defpage-r create {:as arena}
  (mc/insert "arenas" arena)
  (common/layout
   [:h1 "Arena Created!"]
   [:table (arena-details arena)]))
