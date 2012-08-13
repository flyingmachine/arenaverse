(ns arenaverse.views.admin.fighters
  (:require [arenaverse.views.common :as common]
            [arenaverse.data-mappers.fighter :as fighter]
            [arenaverse.data-mappers.arena :as arena]
            [arenaverse.models.permissions :as permissions]
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
  (let [arena (arena/one-by-id (:arena-id fighter))]
    (res/redirect (url-for-r :admin/arenas/show {:shortname (:shortname arena)}))))

(defpage-r create {:as fighter}
  (permissions/protect
   (permissions/modify-fighter? fighter)
   (fighter/create fighter)
   (redirect-to-arena fighter)))

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
   [:td (text-field :team (:team record))]]
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
  (let [fighter (fighter/one-by-id _id)]
    (permissions/protect
     (permissions/modify-fighter? fighter)
     (common/admin-layout
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
               (submit-button "Delete Fighter"))))))

;; TODO possibly do nested route so arena id is present
(defpage-r update {:as fighter-input}
  (let [fighter-id (:_id fighter-input)
        fighter (fighter/one-by-id fighter-id)]
    (permissions/protect
     (permissions/modify-fighter? fighter)
     (let [record (fighter/update fighter-id (dissoc fighter-input :_id))]
       (session/flash-put! "Fighter updated!")
       (redirect-to-arena record)))))

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
    [:div.team (:team record)]]])

(defpartial thumbs [& [query-doc]]
  (map (fn thumb-row [records]
         (into [:div.row] (map thumb records)))
       (partition-all 4 (fighter/all query-doc))))
