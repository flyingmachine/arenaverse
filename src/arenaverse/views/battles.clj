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
(defn random-teamless-fighters [fighters]
  (let [randomer #(rand-int (count fighters))
        left (randomer)
        right (first (filter #(not= % left) (repeatedly randomer)))]
    [(nth fighters left) (nth fighters right)]))

(defn random-team-fighters [fighters]
  (let [teams (vals (group-by :team fighters))
        left (rand-int (count (first teams)))
        right (rand-int (count (second teams)))]
    [(nth (first teams) left) (nth (second teams) right)]))

(defn random-fighters [arena-id]
  (let [fighters (fighter/all {:arena-id arena-id})]
    (if (> (count fighters) 1)
      (if (some #(not (empty? (:team %))) fighters)
        (random-team-fighters fighters)
        (random-teamless-fighters fighters))
      [])))

(defpartial card [record]
  [:div.name (:name record)]
  [:div.pic
   [:a {:href (url-for-r :battles/winner {:_id (fighter/idstr record)})}
    (fighters/fighter-img "battle" record)]])

(defpage-r listing []
  (let [arena (arena/one)
        [left-f right-f] (random-fighters (arena/idstr arena))]
    (session/put! :_ids (map arena/idstr [left-f right-f]))
    (common/layout
     [:h1 (:name arena)]
     [:div.fight-text (:fight-text arena)]
     [:div#battle
      [:div.fighter.a
       (card left-f)]
      [:div.fighter.b
       (card right-f)]])))

(defpage-r winner {:keys [_id]}
  (battle/record-winner (session/get :_ids) _id)
  (battles-listing nil))