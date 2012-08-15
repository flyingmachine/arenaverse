(ns arenaverse.views.admin.arenas
  (:require [arenaverse.views.common :as common]
            [arenaverse.views.admin.fighters :as fighters]
            [arenaverse.data-mappers.fighter :as fighter]
            [arenaverse.data-mappers.arena :as arena]
            [arenaverse.models.permissions :as permissions]
            [noir.session :as session]
            [noir.response :as res]
            [cemerick.friend :as friend]
            [noir.validation :as vali])
  
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        arenaverse.views.routes))

(defn valid? [{:keys [name fight-text]}]
  (vali/rule (vali/has-value? name)
             [:name "You must enter a name"])
  (vali/rule (vali/has-value? fight-text)
             [:fight-text "You must enter some fight text"])
  (vali/rule (vali/max-length? fight-text 35)
             [:fight-text (str "Max 35 characters. You've entered " (count fight-text))])
  (not (vali/errors? :name :fight-text)))

(defpartial error-item [[first-error]]
  [:p.error first-error])

(defpartial arena-fields [{:keys [name fight-text]}]
  [:div
   [:div.control-group
    (vali/on-error :name error-item)
    (label "name" "Name")
    [:span.help "what you'll see when admin'ing your arena"]
    [:div.controls (text-field "name" name)]]
   [:div.control-group
    (vali/on-error :fight-text error-item)
    (label "fight-text" "Fight Text")
    [:span.help "The text above the battling photos, e.g.: &quot;Which creature is scarier?&quot;. Max 35 characters"]
    [:div.controls (text-field "fight-text" fight-text)]]])

(defpartial arena-details [{:keys [name fight-text shortname]}]
  [:tr
   [:td name]
   [:td fight-text]
   [:td [:a {:href (url-for-r :admin/arenas/show {:shortname shortname})} "Edit"]]
   [:td [:a {:href (url-for-r :battles/arena {:shortname shortname})} "View"]]])

(defpage-r listing []
  (common/admin-layout
   [:h1 "Your Arenas"]
   [:p
    [:a.new-arena {:href (url-for-r :admin/arenas/shiny)} "Create New Arena"]]
   [:table.listing
    (map arena-details (arena/by-user (:_id (friend/current-authentication))))]))

(defpage-r shiny {:as arena}
  (common/admin-layout
   [:h1 "Create an Arena"]
   [:p "Both fields are required."]
   (form-to [:post (url-for-r :admin/arenas/create)]
            [:div
             (arena-fields arena)
             [:div.form-actions (submit-button "Create Arena")]])))

(defpage-r edit {:keys [shortname]}
  (let [arena (arena/one {:shortname shortname})]
    (permissions/protect
     (permissions/modify-arena? arena)
     (common/admin-layout
      [:h1 "Delete Arena: " (:name arena)]
      [:p "Are you sure you want to delete this arena?"]
      [:p [:a {:href (url-for-r :admin/arenas/show {:shortname shortname})} "Nah I was just kidding"]]
      (form-to [:post (url-for-r :admin/arenas/destroy {:shortname shortname})]
               (submit-button "Yes, Delete Arena"))))))

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
      [:h1
       (:name arena)
       [:a {:href (url-for-r :battles/arena {:shortname shortname})} "View arena"]]
      (if-let [msg (session/flash-get)]
        [:p.info msg])
      [:div#new-fighter
       [:h2 "New Fighter"]
       (form-to {:enctype "multipart/form-data"}
                [:post (url-for-r :admin/fighters/create)]
                (hidden-field :arena-id (:_id arena))
                [:div
                 (fighters/fighter-fields {} (:_id arena))
                 [:div.form-actions (submit-button "Create Fighter")]])]
      [:div#edit-arena
       [:h2 "Edit Arena"]
       (form-to [:post (url-for-r :admin/arenas/update {:shortname shortname})]
                [:div
                 (arena-fields arena)
                 [:div.form-actions (submit-button "Update Arena")]])


       [:p [:a {:href (url-for-r :admin/arenas/edit arena)} "Delete arena (you will be asked to confirm)"]]]
      [:hr]
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
  (if (valid? arena)
    (let [new-arena (arena/create arena)]
      (res/redirect (url-for-r :admin/arenas/show {:shortname (:shortname new-arena)})))
    (render admin-arenas-shiny arena)))
