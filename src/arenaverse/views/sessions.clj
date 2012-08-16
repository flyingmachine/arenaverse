(ns arenaverse.views.sessions
  (:require [arenaverse.views.common :as common]
            [noir.session :as session]
            [noir.response :as res]
            [cemerick.friend :as friend])
  
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        arenaverse.views.routes)

  (:import [org.bson.types ObjectId]))

(defpage-r new {:keys [username login_failed]}
  (common/layout
   [:h1 "Log In"]
   (if login_failed
     [:p "Uh oh! Your login failed!"])
   (form-to [:post (url-for-r :sessions/new)]
            [:table
             [:tr
              [:td "Username"]
              [:td (text-field "username" username)]]
             [:tr
              [:td "Password"]
              [:td (password-field "password")]]
             [:tr [:td] [:td (submit-button "Log In!!!")]]])
   [:p "If you don't remember your username or password please send a direct tweet to @omgsmackdown and we'll work things out"]))

(defn clear-identity [response]
  (update-in response [:session] dissoc ::identity))

(defpage-r destroy []
  (clear-identity (res/redirect (url-for-r :battles/listing))))