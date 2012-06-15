(ns brainsinvats.views.common
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpartial layout [& content]
  (html5
   [:head
    [:title "brainsinvats"]
    (include-css "/stylesheets/screen/ie.css" "/stylesheets/screen.css")]
   [:body
    [:div#brainsinvats
     [:header
      [:h1 "Brains In Vats"]]
     [:nav
      [:a {:href "/arenas/new"} "New Arena"]]
     content]]))
