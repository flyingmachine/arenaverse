(ns arenaverse.views.moderate.arenas
  (:require [arenaverse.views.common :as common]
            [arenaverse.views.moderate.fighters :as fighters]
            [arenaverse.data-mappers.fighter :as fighter]
            [arenaverse.data-mappers.arena :as arena]
            [arenaverse.models.permissions :as permissions]
            [noir.session :as session]
            [noir.response :as res]
            [cemerick.friend :as friend])
  
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        arenaverse.views.routes))

(defpartial arena-details [{:keys [name fight-text shortname hidden]}]
  [:tr
   [:td [:a {:href (url-for-r :moderate/arenas/show {:shortname shortname})} name]]
   [:td fight-text]
   (if hidden
     [:td [:a {:href (url-for-r :moderate/arenas/unhide {:shortname shortname})} "Unhide"]]
     [:td [:a {:href (url-for-r :moderate/arenas/hide {:shortname shortname})} "Hide"]])])

(defpage-r listing []
  (permissions/protect
   (permissions/moderate-arenas?)
   (common/admin-layout
    [:h1 "Moderate Arenas"]
    (if-let [msg (session/flash-get)]
      [:p.info msg])
    [:table
     (map arena-details (arena/all))])))

(defpage-r show {:keys [shortname]}
  (let [arena (arena/one {:shortname shortname})]
    (permissions/protect
     (permissions/moderate-arena? arena)
     (common/admin-layout
      [:h1 (:name arena)]
      (if-let [msg (session/flash-get)]
        [:p.info msg])
      [:p (:fight-text arena)]
      
      [:div#fighters
       [:h2 "Fighters"]
       (fighters/thumbs {:arena-id (:_id arena)})]))))

(defpage-r hide {:keys [shortname]}
  (let [arena (arena/one {:shortname shortname})]
    (permissions/protect
     (permissions/moderate-arena? arena)
     (arena/update (:_id arena) {:hidden true})
     (session/flash-put! "Arena hidden!")
     (res/redirect (url-for-r :moderate/arenas/listing)))))

(defpage-r unhide {:keys [shortname]}
  (let [arena (arena/one {:shortname shortname})]
    (permissions/protect
     (permissions/moderate-arena? arena)
     (arena/unset (:_id arena) :hidden)
     (session/flash-put! "Arena unhidden!")
     (res/redirect (url-for-r :moderate/arenas/listing)))))
