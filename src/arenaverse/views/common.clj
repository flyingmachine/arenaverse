(ns arenaverse.views.common
  (:require [cemerick.friend :as friend])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        arenaverse.views.routes))


(defpartial pub-nav []
  [:ul#utility
   [:li [:a {:href (url-for-r :users/shiny)} "Create Your Own Smackdown!"]]
   [:li [:a {:href "/login"} "Sign In"]]])

(defpartial admin-nav []
  [:ul#utility
   [:li [:a {:href (url-for-r :admin/arenas/listing)} "Your Arenas"]]
   [:li [:a {:href (url-for-r :admin/arenas/shiny)} "New Arena"]]
   [:li [:a {:href "/logout"} "Log Out"]]])

(defpartial common-layout [nav body-id & [content]]
  (html5
   [:head
    [:title "OMG! SMACKDOWN!!!"]
    [:link {:href "http://fonts.googleapis.com/css?family=Bangers|Nothing+You+Could+Do" :rel "stylesheet" :type "text/css"}]
    (include-css
     "/stylesheets/screen/ie.css"
     "/stylesheets/screen.css")]
   [:body {:id body-id}
    [:div#banner
     [:header
      [:a#logo-link {:href "/"}
       [:img {:src "/img/_ui/logo.png"}]]
      [:nav
       [:ul
        [:li [:a {:href "/"} "Home!"]]
        [:li [:a {:href "http://twitter.com/omgsmackdown"} "Twitter!"]]]
       (if (friend/current-authentication)
         (admin-nav)
         (pub-nav))]]]
    [:div#arenaverse     
     [:div#main
      content]]]))

(defpartial layout [& content]
  (common-layout pub-nav "" content))

(defpartial admin-layout [& content]
  (common-layout admin-nav "admin" content))