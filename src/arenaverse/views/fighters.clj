(ns arenaverse.views.fighters
  (:require [arenaverse.views.common :as common]
            [arenaverse.models.fighter :as fighter]
            [noir.response :as res]
            [monger.collection :as mc])
  
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers
        arenaverse.views.routes)

  (:import [org.bson.types ObjectId]))

(defpage-r create {:as fighter}
  (fighter/create-fighter fighter)
  (res/redirect (url-for-r :arenas/show {:_id (:arena-id fighter)})))

(defpartial fighter-fields [{:keys [name bio]}]
  [:tr
   [:td (label :name "Name")]
   [:td (text-field :name)]]
  [:tr
   [:td (label :bio "Bio")]
   [:td (text-field :bio)]]
  [:tr
   [:td (label :file "Pic")]
   [:tqd (file-upload :file)]])



(defpage-r edit {:keys [_id]}
  (let [fighter (mc/find-map-by-id "fighters" (ObjectId. _id))]
    (common/layout
     [:h2 "Editing Fighter: " (:name fighter)]
     (form-to [:post (url-for-r  :fighters/update {:_id _id})]
            (fighter-fields fighter)
            [:p (submit-button "Update Arena")]))))

(defpartial thumb [record]
  [:div.fighter
   [:div.card
    [:div.name
     [:a {:href (url-for-r :fighters/edit record)} (:name record)]]
    [:div.pic
     [:img {:src  (fighter/amazon-image-path record)}]]
    [:div.bio (:bio record)]]])

(defpartial thumbs []
  (map (fn thumb-row [records]
         (into [:div.row] (map thumb records)))
       (partition 4 (fighter/all))))
