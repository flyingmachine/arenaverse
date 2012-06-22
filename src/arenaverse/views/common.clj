(ns arenaverse.views.common
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        arenaverse.views.routes))

(defpartial layout [& content]
  (html5
   [:head
    [:title "arenaverse"]
    [:link {:href "http://fonts.googleapis.com/css?family=Bangers|Nothing+You+Could+Do" :rel "stylesheet" :type "text/css"}]
    (include-css
     "/stylesheets/screen/ie.css"
     "/stylesheets/screen.css")]
   [:body
    [:div#banner
     [:header
      [:img {:src "/img/_ui/logo.png"}]
      [:nav
       [:ul
        [:li [:a {:href (url-for-r :admin/arenas/listing)} "Arenas"]]        
        [:li [:a {:href (url-for-r :admin/arenas/shiny)} "New Arena"]]]]]]
    [:div#arenaverse
     
     [:div#main
      content]]]))
