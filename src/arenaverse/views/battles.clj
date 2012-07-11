(ns arenaverse.views.battles
  (:require [arenaverse.views.common :as common]
            [arenaverse.views.admin.fighters :as fighters]
            [arenaverse.data-mappers.fighter :as fighter]
            [arenaverse.data-mappers.arena :as arena]
            [arenaverse.data-mappers.battle :as battle]
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

(defpartial card [record, img-version]
  [:div.name (:name record)]
  [:div.pic
   [:a {:href (url-for-r :battles/winner {:_id (:_id record)})}
    (fighters/fighter-img img-version record)]])

(defn win-ratio [fighter wins]
  (let [bouts (reduce + (vals wins))
        _id (keyword (:_id fighter))
        ratio (* 100 (if (= 0 bouts) 1 (/ (_id wins) bouts)))]
    [:div.ratio-card
     (card fighter "card")
     [:div.win-ratio (str (format "%.1f" (double ratio)) "%")]]))

(defn register-fighters! [fighter-a fighter-b]
  (let [fids (into [] (map :_id [fighter-a fighter-b]))]
    (session/put! :battles
                  (merge (session/get :battles)
                         {(keyword (first fids)) fids
                          (keyword (last fids)) fids}))))

(defpartial minor-battle [arena]
  (let [[left-f right-f] (random-fighters (arena/idstr arena))]
    (when (and left-f right-f)
      (register-fighters! left-f right-f)
      [:div.battle
       [:h2 (:fight-text arena)]
       [:div.fighter.a (card left-f "card")]
       [:div.fighter.b (card right-f "card")]])))

(defpartial minor-battles [arenas]
  (loop [html [:div#minor-battles]
         remaining-arenas arenas]
    (if (empty? remaining-arenas)
      html
      (let [arena (first remaining-arenas)]
        (if-let [minor-battle-html (minor-battle arena)]
          (recur (conj html minor-battle-html)
                 (rest remaining-arenas))
          (recur html (rest remaining-arenas)))))))

(defn clear-fighters! []
  (session/put! :battles {}))

;; TODO using apply here is really ugly
(defpage-r listing [& [prev-fighter-id-a prev-fighter-id-b]]
  (clear-fighters!)
  (let [arenas (shuffle (into [] (arena/all)))
        [arena, minor-arenas] [(first arenas) (rest arenas)]]
    (when arena
      (apply common/layout 
             (let [[left-f right-f] (random-fighters (arena/idstr arena))]
               (register-fighters! left-f right-f)
               [[:h1 (:fight-text arena)]
                (when (and left-f right-f)
                  [:div#battle
                   [:div.fighter.a
                    (card left-f "battle")]
                   [:div.fighter.b
                    (card right-f "battle")]
                   (when (and prev-fighter-id-a prev-fighter-id-b)
                     (let [previous-fighters (map #(fighter/one-by-id %) [prev-fighter-id-a prev-fighter-id-b])
                           wins (battle/record-for-pair (map :_id previous-fighters))]
                       [:div.win-ratios
                        [:h2 "Win Ratio"]
                        (win-ratio (first previous-fighters) wins)
                        (win-ratio (second previous-fighters) wins)]))])
                (minor-battles minor-arenas)])))))

(defpage-r winner {:keys [_id]}
  (let [previous-fighter-ids ((keyword _id) (session/get :battles))]
    (battle/record-winner! previous-fighter-ids _id)
    (println previous-fighter-ids)
    (battles-listing previous-fighter-ids)))