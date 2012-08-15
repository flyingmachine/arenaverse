(ns arenaverse.views.users
  (:require [arenaverse.views.common :as common]
            [arenaverse.data-mappers.user :as user]
            [arenaverse.models.permissions :as can]
            [noir.session :as session]
            [noir.response :as res]
            [cemerick.friend :as friend]
            [cemerick.friend.workflows :as workflows]
            [noir.validation :as vali])
  
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        arenaverse.views.routes))

(defn valid? [{:keys [username password]}]
  (vali/rule (vali/min-length? username 4)
             [:username "Your username must be at least 4 characters"])
  (vali/rule (nil? (user/one {:username username}))
             [:username "That username is already taken :("])
  (vali/rule (vali/min-length? password 4)
             [:password "Your password must be at least 4 characters"])
  (not (vali/errors? :username :password)))

(defpartial error-item [[first-error]]
  [:p.error first-error])

(defpage-r shiny {:as user}
  (common/layout
   [:h1 "Sign Up!"]
   [:p "Wow, you are about to make one of the best decisions of your life. Congratulations!"]
   (form-to [:post (url-for-r :users/create)]
            [:div
             [:div.control-group

              (label "username" "Username")
              [:span.help "Must be at least 4 characters"]
              [:div.controls (text-field "username" (:username user))]]
             [:div.control-group
              (vali/on-error :password error-item)
              (label "password" "Password")
              [:span.help "Must be at least 4 characters"]
              [:div.controls (password-field "password")]]
             [:div.form-controls (submit-button "Sign Up")]])))


(defpage-r create {:as user}
  (if (valid? user)
    (do
      (user/create user)
      (workflows/make-auth (user/one {:username (:username user)}))
      (res/redirect (url-for-r :admin/arenas/listing)))
    (render users-shiny user)))