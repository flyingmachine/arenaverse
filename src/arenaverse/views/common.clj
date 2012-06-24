(ns arenaverse.views.common
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        arenaverse.views.routes))


(defpartial pub-nav []
  [:ul
   [:li [:a {:href "/"} "Home!"]]
   [:li [:a {:href "http://twitter.com/omgsmackdown"} "Twitter!"]]])

(defpartial admin-nav []
  [:ul
   [:li [:a {:href (url-for-r :admin/arenas/listing)} "Arenas"]]        
   [:li [:a {:href (url-for-r :admin/arenas/shiny)} "New Arena"]]])

(defpartial common-layout [nav & [content]]
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
      [:a {:href "/"}
       [:img {:src "/img/_ui/logo.png"}]]
      [:nav
       (nav)]]]
    [:div#arenaverse     
     [:div#main
      content]]]))

(defpartial layout [& content]
  (common-layout pub-nav content))

(defpartial admin-layout [& content]
  (common-layout admin-nav content))