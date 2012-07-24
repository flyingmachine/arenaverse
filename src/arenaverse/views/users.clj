(ns arenaverse.views.users
  (:require [arenaverse.views.common :as common]
            [arenaverse.data-mappers.user :as user]
            [arenaverse.models.permissions :as can]
            [noir.session :as session]
            [noir.response :as res]
            [cemerick.friend :as friend]
            [cemerick.friend.workflows :as workflows])
  
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        arenaverse.views.routes))

(defpage-r shiny {:as user}
  (common/layout
   [:h1 "Sign Up!"]
   [:p "Wow, you are about to make one of the best decisions of your life. Congratulations!"]
   (form-to [:post (url-for-r :users/create)]
            [:table
             [:tr
              [:td (label "username" "Username")]
              [:td (text-field "username" (:username user))]]
             [:tr
              [:td (label "password" "Password")]
              [:td (password-field "password")]]
             [:tr [:td] [:td (submit-button "Sign Up")]]])))


(defpage-r create {:as user}
  (user/create user)
  (workflows/make-auth (user/one {:username (:username user)}))
  (res/redirect (url-for-r :admin/arenas/listing)))