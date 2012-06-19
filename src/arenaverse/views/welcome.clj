(ns arenaverse.views.welcome
  (:require [arenaverse.views.common :as common])
  
  (:use [noir.core :only [defpage]]
        [hiccup.core :only [html]]))

(defpage "/" []
  (common/layout
   [:p "Arenaverse! Arrsafrsafa!"]))
