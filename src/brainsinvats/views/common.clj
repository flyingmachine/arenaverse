(ns brainsinvats.views.common
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        brainsinvats.views.routes))

(defpartial layout [& content]
  (html5
   [:head
    [:title "brainsinvats"]
    (include-css
     "http://fonts.googleapis.com/css?family=Just+Another+Hand"
     "/stylesheets/screen/ie.css"
     "/stylesheets/screen.css")]
   [:body
    [:div#brainsinvats
     [:header
      [:h1 "Arenaverse!"]]
     [:nav
      [:a {:href (url-forr :arenas/shiny)} "New Arena"]]
     content]]))
