(ns arenaverse.views.welcome
  (:require [arenaverse.views.common :as common]
            [noir.content.pages :as pages])
  
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage "/" []
  (common/layout
   [:p "Arenaverse! Arrsafrsafa!"]))
