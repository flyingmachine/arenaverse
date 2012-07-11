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

;; TODO use shuffle
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

(defpartial _minor-battle [battle]
  (let [[left-f right-f] (:fighters battle)]
    (when (and left-f right-f)
      [:div.battle
       [:h2 (:fight-text (:arena battle))]
       [:div.fighter.a (card left-f "card")]
       [:div.fighter.b (card right-f "card")]])))

(defpartial _minor-battles [minor-battles]
  (loop [html [:div#minor-battles]
         remaining-battles minor-battles]
    (if (empty? remaining-battles)
      html
      (let [battle (first remaining-battles)]
        (if-let [minor-battle-html (_minor-battle battle)]
          (recur (conj html minor-battle-html)
                 (rest remaining-battles))
          (recur html (rest remaining-battles)))))))

(defn clear-battles! []
  (session/put! :battles {}))

(defn register-battles! [b]
  (let [battles-processed (reduce (fn [m {:keys [arena fighters]}]
                                    (let [fids (into [] (map fighter/idstr fighters))
                                          aid (arena/idstr arena)]
                                      (assoc m (first fids) (conj fids aid)
                                               (last fids) (conj fids aid))))
                                  {}
                                  b)]
    (session/put! :battles battles-processed)
    (session/put! :main-battle-fighters (battles-processed (fighter/idstr (first (:fighters (first b))))))))

(defn battles []
  (let [b (shuffle (filter #(not (empty? (:fighters %))) (map (fn [arena] {:arena arena :fighters (random-fighters (arena/idstr arena))}) (arena/all))))]
    (register-battles! b)
    b))

;; TODO using apply here is really ugly
(defpage-r listing []
  (clear-battles!)
  ;; TODO the first assignment has to come before the second. any way
  ;; to get around this?
  (let [[prev-fighter-id-a prev-fighter-id-b] (session/get :main-battle-fighters)
        [main-battle & minor-battles] (battles)]
    (when main-battle
      (apply common/layout 
             (let [[left-f right-f] (:fighters main-battle)]
               [[:h1 (:fight-text (:arena main-battle))]
                [:div#battle
                 [:div.fighter.a
                  (card left-f "battle")]
                 [:div.fighter.b
                  (card right-f "battle")]
                 (when (and prev-fighter-id-a prev-fighter-id-b)
                   (let [previous-fighters (map #(fighter/one-by-id %) [prev-fighter-id-a prev-fighter-id-b])
                         wins (battle/record-for-pair (map :_id previous-fighters))]
                                              (println "MAPPED" previous-fighters)
                     [:div.win-ratios
                      [:h2 "Win Ratio"]
                      (win-ratio (first previous-fighters) wins)
                      (win-ratio (second previous-fighters) wins)]))]
                (_minor-battles minor-battles)])))))

(defpage-r winner {:keys [_id]}
  (let [selected-battle-fighter-ids (take 2 ((session/get :battles) _id))]
    (battle/record-winner! selected-battle-fighter-ids _id)
    ;; TODO why does battles-listing expect an argument here?
    (battles-listing nil)))