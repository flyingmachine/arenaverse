(ns arenaverse.views.battles
  (:require [arenaverse.views.common :as common]
            [arenaverse.views.admin.fighters :as fighters]
            [arenaverse.models.fighter :as fighter]
            [arenaverse.models.arena :as arena]
            [arenaverse.models.battle :as battle]
            [noir.session :as session]
            [monger.collection :as mc])
  
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        arenaverse.views.routes)
  
  (:import [org.bson.types ObjectId]))

;; TODO how to test this?
(defn random-fighters [arena-id]
  (let [fighters (fighter/all {:arena-id arena-id})]
    (if (> (count fighters) 1)
      (let [randomer #(rand-int (count fighters))
            left (randomer)
            right (first (filter #(not= % left) (repeatedly randomer)))]
        [(nth fighters left) (nth fighters right)])
      [])))

(defpage-r listing []
  (let [arena (arena/one)]
    (common/layout
     [:h1 (:name arena)]
     [:div.fight-text (:fight-text arena)]
     (let [[left-f right-f] (random-fighters (.toString (:_id arena)))]
       [:div#battle
        [:div.fighter.a
         [:div.name (:name left-f)]
         [:div.pic (fighters/fighter-img "battle" left-f)]]
        
        [:div.fighter.b
         [:div.name (:name right-f)]
         [:div.pic (fighters/fighter-img "battle" right-f)]]]))))
