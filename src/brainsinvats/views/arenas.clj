(ns brainsinvats.views.arenas
  (:require [brainsinvats.views.common :as common]
            [noir.content.pages :as pages])
  
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        brainsinvats.views.routes))


(defpage-r shiny {}
  (common/layout
   [:p "Welcome to brainsinvats"]))

(defpage-r edit [id])