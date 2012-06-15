(ns arenaverse.views.arenas
  (:require [arenaverse.views.common :as common]
            [noir.content.pages :as pages])
  
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        arenaverse.views.routes))


(defpage-r shiny {}
  (common/layout
   [:h2 "Create an Arena"]))

(defpage-r edit [id])
