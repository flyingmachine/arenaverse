(ns arenaverse.views.admin.fighters
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
  (res/redirect (url-for-r :admin/arenas/show {:_id (:arena-id fighter)})))

(defpage-r create {:as fighter}
  (fighter/create fighter)
  (redirect-to-arena fighter))

(defpartial fighter-img [version record]
  [:img {:src  (fighter/amazon-image-path version record)}])

(defpartial fighter-fields [record]
  [:tr
   [:td (label :name "Name")]
   [:td (text-field :name (:name record))]]
  [:tr
   [:td (label :caption "Caption")]
   [:td (text-field :caption (:caption record))]]
  [:tr
   [:td (label :bio "Team")]
   [:td (text-field :bio (:team record))]]
  [:tr
   [:td (label :bio "Bio")]
   [:td (text-area :bio (:bio record))]]
  [:tr
   [:td (label :file "Pic")]
   [:td
    (file-upload :file)
    [:br]
    (if (:_id record)
      (fighter-img "card" record))]])

(defpage-r edit {:keys [_id]}
  (let [fighter (mc/find-map-by-id "fighters" (ObjectId. _id))]
    (common/layout
     [:h1 "Editing Fighter: " (:name fighter)]
     (form-to {:enctype "multipart/form-data"}
              [:post (url-for-r  :admin/fighters/update {:_id _id})]
              [:table
               (fighter-fields fighter)
               [:tr
                [:td]
                [:td (submit-button "Update Fighter")]]])
     (form-to [:post (url-for-r :admin/fighters/destroy {:_id _id})]
              (hidden-field :arena-id (:arena-id fighter))
              (submit-button "Delete Fighter")))))

;; TODO possibly do nested route so arena id is present
(defpage-r update {:as fighter}
  (let [record (fighter/update fighter)]
    (session/flash-put! "Fighter updated!")
    (redirect-to-arena record)))

(defpage-r destroy {:keys [_id arena-id]}
  (fighter/destroy _id)
  (redirect-to-arena {:arena-id arena-id}))

(defpartial thumb [record]
  [:div.fighter
   [:div.card
    [:div.name
     [:a {:href (url-for-r :admin/fighters/edit record)} (:name record)]]
    [:div.pic
     (fighter-img "card" record)]
    [:div.caption (:caption record)]]])

(defpartial thumbs [& [query-doc]]
  (map (fn thumb-row [records]
         (into [:div.row] (map thumb records)))
       (partition-all 4 (fighter/all query-doc))))
