(ns brainsinvats.views.arenas
  (:require [brainsinvats.views.common :as common]
            [noir.content.pages :as pages])
  
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        brainsinvats.views.routes))


(defpage-r shiny {}
  (common/layout
   [:h2 "Create an Arena"]))

(defpage-r edit [id])