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
  [:tr.fighter
   [:td.pic (fighter-img "listing" record)]
   [:td.name (:name record)]
   (if (:hidden record)
     [:td [:a {:href (url-for-r :moderate/fighters/unhide {:_id (:_id record)})} "Unhide"]]
     [:td [:a {:href (url-for-r :moderate/fighters/hide {:_id (:_id record)})} "Hide"]])])

(defpartial thumbs [& [query-doc]]
  (let [fighters (fighter/all query-doc)]
    [:table.moderate
     (map thumb fighters)]))

(defpage-r hide {:keys [_id]}
  (let [fighter (fighter/one {:_id _id})]
    (permissions/protect
     (permissions/moderate-fighter? fighter)
     (fighter/update _id {:_id _id, :hidden true})
     (session/flash-put! "Fighter hidden!")
     (res/redirect (url-for-r :moderate/arenas/listing)))))

(defpage-r unhide {:keys [_id]}
  (let [fighter (fighter/one {:_id _id})]
    (permissions/protect
     (permissions/moderate-fighter? fighter)
     (fighter/unset _id :hidden)
     (session/flash-put! "Fighter unhidden!")
     (res/redirect (url-for-r :moderate/arenas/listing)))))
