(ns arenaverse.views.arenas
  (:require [arenaverse.views.common :as common]
            [noir.content.pages :as pages])
  
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers
        arenaverse.views.routes))

(defpartial arena-fields [{:keys [name description]}]
  [:table
   [:tr
    [:td (label "name" "Name: ")]
    [:td (text-field "name" name)]]
   [:tr
    [:td (label "description" "Description: ")]
    [:td (text-field "description" description)]]])

(defpage-r shiny {:as arena}
  (common/layout
   [:h2 "Create an Arena"]
   (form-to [:post "/arenas"]
            (arena-fields arena)
            (submit-button "Create Arena"))))

(defpage-r create {:as arena})

(defpage-r edit [id])
