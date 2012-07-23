(ns arenaverse.views.admin.arenas
  (:require [arenaverse.views.common :as common]
            [arenaverse.views.admin.fighters :as fighters]
            [arenaverse.data-mappers.fighter :as fighter]
            [arenaverse.data-mappers.arena :as arena]
            [arenaverse.models.permissions :as can]
            [noir.session :as session]
            [noir.response :as res]
            [cemerick.friend :as friend])
  
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        arenaverse.views.routes))

(defpartial arena-fields [{:keys [name fight-text]}]
  [:tr
    [:td (label "name" "Name")]
    [:td (text-field "name" name)]]
   [:tr
    [:td (label "fight-text" "Fight Text")]
    [:td (text-field "fight-text" fight-text)]])

(defpartial arena-details [{:keys [name fight-text _id]}]
  [:tr
   [:td [:a {:href (url-for-r :admin/arenas/show {:_id _id})} name]]
   [:td fight-text]])

(defpage-r listing []
  (common/admin-layout
   [:h1 "Arenas"]
   [:table
    (map arena-details (arena/by-user (:_id (friend/current-authentication))))]))

(defpage-r shiny {:as arena}
  (common/admin-layout
   [:h1 "Create an Arena"]
   (form-to [:post (url-for-r :admin/arenas/create)]
            [:table
             (arena-fields arena)
             [:tr [:td] [:td (submit-button "Create Arena")]]])))

(defpage-r edit {:keys [_id]}
  (let [arena (arena/one-by-id _id)]
    (can/protect
     (can/modify_arena? arena)
     (common/admin-layout
      [:h1 "Editing Arena: " (:name arena)]
      (form-to [:post (url-for-r :admin/arenas/update {:_id _id})]
               [:table
                (arena-fields arena)
                [:tr [:td] [:td (submit-button "Update Arena")]]])
      (form-to [:post (url-for-r :admin/arenas/destroy {:_id _id})]
               (submit-button "Delete Arena"))))))

(defpage-r destroy {:keys [_id]}
  (can/protect
   (can/modify_arena? (arena/one-by-id _id))
   (arena/destroy _id)
   (res/redirect (url-for-r :admin/arenas/listing))))

(defpage-r show {:keys [_id]}
  (let [arena (arena/one-by-id _id)]
    (can/protect
     (can/modify_arena? arena)
     (common/admin-layout
      [:h1 (:name arena)]
      (if-let [msg (session/flash-get)]
        [:p.info msg])
      [:p [:a {:href (url-for-r :admin/arenas/edit arena)} "Edit"]]
      [:p (:fight-text arena)]

      [:div#new-fighter
       [:h2 "New Fighter"]
       (form-to {:enctype "multipart/form-data"}
                [:post (url-for-r :admin/fighters/create)]
                (hidden-field :arena-id _id)
                [:table
                 (fighters/fighter-fields {})
                 [:tr
                  [:td]
                  [:td (submit-button "Create Fighter")]]])]
      
      [:div#fighters
       [:h2 "Fighters"]
       (fighters/thumbs {:arena-id _id})]))))

;; todo put name and fight-text in separate map?
(defpage-r update {:keys [_id name fight-text]}
  (can/protect
   (can/modify_arena? (arena/one-by-id _id))
   (arena/update _id {:name name :fight-text fight-text})
   (session/flash-put! "Arena updated!")
   (admin-arenas-show {:_id _id})))

(defpage-r create {:as arena}
  (arena/create arena)
  (res/redirect (url-for-r :admin/arenas/listing)))
