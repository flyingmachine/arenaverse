(ns arenaverse.views.moderate.fighters
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

(defpartial fighter-img [version record]
  [:img {:src  (fighter/amazon-image-path version record)}])

;; TODO possibly do nested route so arena id is present
;; (defpage-r update {:as fighter-input}
;;   (let [fighter (fighter/one-by-id (:_id fighter-input))]
;;     (permissions/protect
;;      (permissions/modify-fighter? fighter)
;;      (let [record (fighter/update fighter-input)]
;;        (session/flash-put! "Fighter updated!")
;;        (redirect-to-arena record)))))

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
