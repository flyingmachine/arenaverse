(ns brainsinvats.views.welcome
  (:require [brainsinvats.views.common :as common]
            [noir.content.pages :as pages])
  
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage "/" []
  (common/layout
   [:p "Welcome to brainsinvats"]
   [:p "You're really going to enjoy your time here"]))
