(ns arenaverse.views.fighters
  (:require [arenaverse.views.common :as common]
            [arenaverse.models.fighter :as fighter]
            [noir.response :as res])
  
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers
        arenaverse.views.routes))

(defpage-r create {:as fighter}
  (fighter/create-fighter fighter)
  (res/redirect (url-for-r :arenas/show {:id (:arena-id fighter)})))