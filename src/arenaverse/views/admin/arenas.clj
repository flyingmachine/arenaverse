(ns arenaverse.views.admin.arenas
  (:require [arenaverse.views.common :as common]
            [arenaverse.views.admin.fighters :as fighters]
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

(defpartial arena-fields [{:keys [name fight-text]}]
  [:tr
    [:td (label "name" "Name")]
    [:td (text-field "name" name)]]
   [:tr
    [:td (label "fight-text" "Fight Text")]
    [:td (text-field "fight-text" fight-text)]])

(defpartial arena-details [{:keys [name fight-text shortname]}]
  [:tr
   [:td [:a {:href (url-for-r :admin/arenas/show {:shortname shortname})} name]]
   [:td fight-text]])

(defpage-r listing []
  (common/admin-layout
   [:h1 "Your Arenas"]
   [:p
    [:a {:href (url-for-r :admin/arenas/shiny)} "New Arena"]]
   [:table
    (map arena-details (arena/by-user (:_id (friend/current-authentication))))]))

(defpage-r shiny {:as arena}
  (common/admin-layout
   [:h1 "Create an Arena"]
   (form-to [:post (url-for-r :admin/arenas/create)]
            [:table
             (arena-fields arena)
             [:tr [:td] [:td (submit-button "Create Arena")]]])))

(defpage-r edit {:keys [shortname]}
  (let [arena (arena/one {:shortname shortname})]
    (permissions/protect
     (permissions/modify-arena? arena)
     (common/admin-layout
      [:h1 "Editing Arena: " (:name arena)]
      (form-to [:post (url-for-r :admin/arenas/update {:shortname shortname})]
               [:table
                (arena-fields arena)
                [:tr [:td] [:td (submit-button "Update Arena")]]])
      (form-to [:post (url-for-r :admin/arenas/destroy {:shortname shortname})]
               (submit-button "Delete Arena"))))))

(defpage-r destroy {:keys [shortname]}
  (permissions/protect
   (permissions/modify-arena? (arena/one {:shortname shortname}))
   (arena/destroy shortname)
   (res/redirect (url-for-r :admin/arenas/listing))))

(defpage-r show {:keys [shortname]}
  (let [arena (arena/one {:shortname shortname})]
    (permissions/protect
     (permissions/modify-arena? arena)
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
                (hidden-field :arena-id (:_id arena))
                [:table
                 (fighters/fighter-fields {})
                 [:tr
                  [:td]
                  [:td (submit-button "Create Fighter")]]])]
      
      [:div#fighters
       [:h2 "Fighters"]
       (fighters/thumbs {:arena-id (:_id arena)})]))))

;; todo put name and fight-text in separate map?
(defpage-r update {:keys [shortname name fight-text]}
  (let [arena (arena/one {:shortname shortname})]
    (permissions/protect
     (permissions/modify-arena? arena)
     (arena/update (:_id arena) {:name name :fight-text fight-text})
     (session/flash-put! "Arena updated!")
     (admin-arenas-show {:shortname shortname}))))

(defpage-r create {:as arena}
  (arena/create arena)
  (res/redirect (url-for-r :admin/arenas/listing)))
