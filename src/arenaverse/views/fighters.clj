(ns arenaverse.views.fighters
  (:require [arenaverse.views.common :as common]
            [arenaverse.models.fighter :as fighter]
            [noir.response :as res]
            [noir.session :as session]
            [monger.collection :as mc])
  
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers
        arenaverse.views.routes)

  (:import [org.bson.types ObjectId]))

(defn redirect-to-arena [fighter]
  (res/redirect (url-for-r :arenas/show {:_id (:arena-id fighter)})))

(defpage-r create {:as fighter}
  (fighter/create-fighter fighter)
  (redirect-to-arena fighter))

(defpartial fighter-fields [{:keys [name bio]}]
  [:tr
   [:td (label :name "Name")]
   [:td (text-field :name name)]]
  [:tr
   [:td (label :bio "Bio")]
   [:td (text-field :bio bio)]]
  [:tr
   [:td (label :file "Pic")]
   [:td (file-upload :file)]])

(defpage-r edit {:keys [_id]}
  (let [fighter (mc/find-map-by-id "fighters" (ObjectId. _id))]
    (common/layout
     [:h2 "Editing Fighter: " (:name fighter)]
     (form-to {:enctype "multipart/form-data"}
              [:post (url-for-r  :fighters/update {:_id _id})]
              [:table
               (fighter-fields fighter)
               [:tr
                [:td]
                [:td (submit-button "Update Fighter")]]]))))

(defpage-r update {:as fighter}
  (let [record (fighter/update-fighter fighter)]    
    (session/flash-put! "Fighter updated!")
    (redirect-to-arena record)))

(defpartial thumb [record]
  [:div.fighter
   [:div.card
    [:div.name
     [:a {:href (url-for-r :fighters/edit record)} (:name record)]]
    [:div.pic
     [:img {:src  (fighter/amazon-image-path "original" record)}]]
    [:div.bio (:bio record)]]])

(defpartial thumbs [& [query-doc]]
  (map (fn thumb-row [records]
         (into [:div.row] (map thumb records)))
       (partition-all 4 (fighter/all query-doc))))
