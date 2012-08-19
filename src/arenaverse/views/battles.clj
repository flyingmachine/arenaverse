(ns arenaverse.views.battles
  (:require [arenaverse.views.common :as common]
            [arenaverse.views.admin.fighters :as fighters]
            [arenaverse.data-mappers.fighter :as fighter]
            [arenaverse.data-mappers.arena :as arena]
            [arenaverse.data-mappers.battle :as battle]
            [noir.session :as session])
  
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        arenaverse.views.routes
        monger.operators)
  
  (:import [org.bson.types ObjectId]))


;;------
;; Functions for setting up data for displaying a battle page
;;------

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
  (let [fighters (fighter/all {:arena-id arena-id
                               :hidden {$exists false}
                               :image-extension {$exists true $ne ""}})]
    (if (> (count fighters) 1)
      (if (some #(not (empty? (:team %))) fighters)
        (random-team-fighters fighters)
        (random-teamless-fighters fighters))
      [])))


(defn battle->session-battle [battle]
  (let [shortname (:shortname (:arena battle))]
    (conj (map :_id (:fighters battle)) shortname)))

;; Takes a seq of battles, which are a map of arena and two fighters
;; for that arena
(defn register-battles! [b]
  (let [battles-processed (apply hash-map
                                 (reduce (fn [list battle]
                                           (let [bs (battle->session-battle battle)]
                                             (conj list (first bs) bs)))
                                         []
                                         b))]
    (session/put! :battles battles-processed)
    (session/put! :main-battle (battle->session-battle (first b)))))

(defn arena->battle [arena]
  {:arena arena :fighters (random-fighters (arena/idstr arena))})

(defn battle-filter [battles]
  (println (map count (map :fighters battles)))
  (filter #(>= (count (:fighters %)) 2) battles))

(defn filtered-arenas []
  (arena/all {:hidden {$exists false}}))

(defn battles-without-main-arena-specified []
  (shuffle (battle-filter (map arena->battle (filtered-arenas)))))

(defn battles-with-main-arena-specified [main-arena]
  (let [arenas (remove #(= main-arena %) (filtered-arenas))]
    (reverse (conj (shuffle (battle-filter (map arena->battle arenas))) (arena->battle main-arena)))))

(defn battles [main-arena]
  (let [b (if main-arena
            (battles-with-main-arena-specified main-arena)
            (battles-without-main-arena-specified))]
    (register-battles! b)
    b))

;;----------
;; Partials for battle page
;;----------

(defpartial card [arena record img-version]
  [:div.name (:name record)]
  [:div.pic
   [:a {:href (url-for-r :battles/winner {:_id (:_id record) :arena-shortname (:shortname arena)})}
    (fighters/fighter-img img-version record)]])

(defpartial card-without-battle [record img-version]
  [:div.name (:name record)]
  [:div.pic
   (fighters/fighter-img img-version record)])

(defpartial win-ratio [fighter wins]
  (let [bouts (reduce + (vals wins))
        _id (keyword (:_id fighter))
        ratio (* 100 (if (= 0 bouts) 1 (/ (_id wins) bouts)))]
    [:div.ratio-card
     (card-without-battle  fighter "card")
     [:div.win-ratio (str (format "%.1f" (double ratio)) "%")]]))

(defpartial _minor-battle [battle]
  (if battle
    (let [[left-f right-f] (:fighters battle)
          arena (:arena battle)]
      [:div.battle
       [:h2 (:fight-text (:arena battle))]
       [:div.fighter.a (card arena left-f "card")]
       [:div.fighter.b (card arena right-f "card")]])))

(defpartial _minor-battle-row [row]
  (println row)
  [:div.row
   (map _minor-battle row)])

(defpartial _minor-battles [minor-battles]
  (let [rows (partition 2 2 [nil] minor-battles)]
    [:div#minor-battles
     (map _minor-battle-row rows)]))

(defpartial previous-battle-results [prev-fighter-id-a prev-fighter-id-b]
  (when (and prev-fighter-id-a prev-fighter-id-b)
    (let [previous-fighters (map #(fighter/one-by-id %) [prev-fighter-id-a prev-fighter-id-b])
          wins (battle/record-for-pair (map :_id previous-fighters))]
      [:div.win-ratios
       [:h2 "Win Ratio"]
       (win-ratio (first previous-fighters) wins)
       (win-ratio (second previous-fighters) wins)])))

(defpartial main-area [arena left-f right-f]
  [:div#battle
   [:div.fighter.a
    (when left-f (card arena left-f "battle"))]
   [:div.fighter.b
    (when right-f (card arena right-f "battle"))]])

(defpartial battle [{:keys [prev-fighter-id-a
                            prev-fighter-id-b
                            prev-main-arena-shortname
                            main-arena-shortname]}]
  (let [designated-main-battle (when main-arena-shortname (arena/one {:shortname main-arena-shortname}))
        [main-battle & minor-battles] (battles designated-main-battle)]
    (when main-battle
      (let [[left-f right-f] (:fighters main-battle)
            arena (:arena main-battle)]
        (common/layout 
         [:h1 (:fight-text (:arena main-battle))]
         [:div#battles
          (main-area arena left-f right-f)
          (_minor-battles minor-battles)]
         [:div#secondary
          (previous-battle-results prev-fighter-id-a prev-fighter-id-b)
          "<script type='text/javascript'>
var amzn_wdgt={widget:'MyFavorites'};
amzn_wdgt.tag='aflyingmachin-20';
amzn_wdgt.columns='1';
amzn_wdgt.rows='4';
amzn_wdgt.title='OMG! Stuff!!!';
amzn_wdgt.width='120';
amzn_wdgt.ASIN='0061992704,0062113372,1440034044,B0054U53ZS,B0029LHWSQ,1455523429,B005HI4LLY,B0051QVESA,B008G33O0G,B001KVZ6HK,B0084IG7KC,B0044XV3QY,B000FZETI4,B000RPCJBG,B000I0RNVQ,B004SIP8OI,B00003CWOU,B000YN363G,B005LAZK8G';
amzn_wdgt.showImage='True';
amzn_wdgt.showPrice='True';
amzn_wdgt.showRating='True';
amzn_wdgt.design='2';
amzn_wdgt.headerTextColor='#FFFFFF';
amzn_wdgt.marketPlace='US';
amzn_wdgt.outerBackgroundColor='#2493C8';
amzn_wdgt.borderColor='#FFFFFF';
</script>
<script type='text/javascript' src='http://wms.assoc-amazon.com/20070822/US/js/AmazonWidgets.js'>
</script>"])))))

(defn session-battle->battle-map [session-battle]
  (let [[prev-main-arena-shortname prev-fighter-id-a prev-fighter-id-b] session-battle]
    {:prev-main-arena-shortname prev-main-arena-shortname
     :prev-fighter-id-a prev-fighter-id-a
     :prev-fighter-id-b prev-fighter-id-b}))

(defpage-r listing []
  (battle (session-battle->battle-map (session/get :main-battle))))

(defpage-r winner {:keys [arena-shortname _id]}
  (let [selected-battle ((session/get :battles) arena-shortname)
        selected-battle-fighter-ids (rest selected-battle)]
    (battle/record-winner! selected-battle-fighter-ids _id)
    (let [battle-map (session-battle->battle-map (or selected-battle (session/get :main-battle)))]
      (battle (assoc battle-map :main-arena-shortname (:prev-main-arena-shortname battle-map))))))

(defpage-r arena {:keys [shortname]}
  (battle {:main-arena-shortname shortname}))