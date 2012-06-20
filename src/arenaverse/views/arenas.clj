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
   [:td [:a {:href (url-for-r :arenas/show {:id _id})} name]]
   [:td fight-text]])

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
     (if-let [msg (session/flash-get)]
       [:p.info msg])
     [:p [:a {:href (url-for edit {:id id})} "Edit"]]
     [:p (:fight-text arena)]

     [:h3 "Fighters"]
     (map fighters/fighter (fighter/all))
     [:h3 "New Fighter"]
     (form-to {:enctype "multipart/form-data"}
              [:post (url-for-r :fighters/create)]
              (hidden-field :arena-id id)
              [:table
               [:tr
                [:td (label :name "Name")]
                [:td (text-field :name)]]
               [:tr
                [:td (label :bio "Bio")]
                [:td (text-field :bio)]]
               [:tr
                [:td (label :file "Pic")]
                [:td (file-upload :file)]]
               [:tr
                [:td]
                [:td (submit-button "Create Fighter")]]]))))

;; todo put name and fight-text in separate map?
(defpage-r update {:keys [id name fight-text]}
  (mc/update-by-id "arenas" (ObjectId. id) {:name name :fight-text fight-text})
  (session/flash-put! "Arena updated!")
  (show {:id id}))

(defpage-r shiny {:as arena}
  (common/layout
   [:h2 "Create an Arena"]
   (form-to [:post "/arenas"]
            (arena-fields arena)
            [:p (submit-button "Create Arena")])))

(defpage-r create {:as arena}
  (mc/insert "arenas" arena)
  (common/layout
   [:h2 "Arena Created!"]
   [:table (arena-details arena)]))