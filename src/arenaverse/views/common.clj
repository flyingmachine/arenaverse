(ns arenaverse.views.common
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        arenaverse.views.routes))

(defpartial layout [& content]
  (html5
   [:head
    [:title "arenaverse"]
    (include-css
     "http://fonts.googleapis.com/css?family=Bangers"
     "/stylesheets/screen/ie.css"
     "/stylesheets/screen.css")]
   [:body
    [:div#arenaverse
     [:header
      [:img {:src "/img/_ui/logo.png"}]]
     [:nav
      [:ul
       [:li [:a {:href (url-for-r :arenas/shiny)} "New Arena"]]
       [:li [:a {:href "#"} "Another Link"]]]]
     [:div#main
      content]]]))
