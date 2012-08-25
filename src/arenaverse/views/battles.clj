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

;; When there are no teams, it's everybody against everybody
(defn random-teamless-fighters [fighters]
  (let [randomer #(rand-int (count fighters))
        left (randomer)
        right (first (filter #(not= % left) (repeatedly randomer)))]
    [(nth fighters left) (nth fighters right)]))

;; When we're dealing with an arena that has teams, we need to ensure
;; that we don't pit two fighters from the same team against each other
(defn random-team-fighters [fighters]
  (let [randomer #(nth fighters (rand-int (count fighters)))
        left (randomer)
        right (first (filter #(not= (:team %) (:team left)) (repeatedly randomer)))]
    [left right]))

;; Return a random list of fighters for a given arena id
(defn random-fighters [arena-id]
  ;; The image-extension filter ensures that we don't get fighters
  ;; that are missing an image
  (let [fighters (fighter/all {:arena-id arena-id
                               :hidden {$exists false}
                               :image-extension {$exists true $ne ""}})]
    (if (> (count fighters) 1)
      (if (some #(not (empty? (:team %))) fighters)
        (random-team-fighters fighters)
        (random-teamless-fighters fighters))
      [])))

;; Convert a battle record into the format we want to store in the session
(defn battle->session-battle [battle]
  (let [shortname (:shortname (:arena battle))]
    (conj (map :_id (:fighters battle)) shortname)))

;; Takes a seq of battles, which are a map of arena and two fighters
;; for that arena. Save all battles in the session so that we know who
;; the loser was when the user selects a winner.
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

;; Ensure that we only deal with displayable battles
(defn battle-filter [battles]
  (filter #(>= (count (:fighters %)) 2) battles))

;; Arenas can be hidden through moderation
(defn filtered-arenas []
  (arena/all {:hidden {$exists false}}))

;; If the main arena isn't specified we don't have to do anything
;; special to ensure the order of the battles returned
(defn battles-without-main-arena-specified []
  (shuffle (battle-filter (map arena->battle (filtered-arenas)))))

;; When the main arena is specified, then the battle in that arena
;; needs to be at the head of the seq returned. This is because the
;; battle partial designates the first battle as the "main" one
(defn battles-with-main-arena-specified [main-arena]
  (let [arenas (remove #(= main-arena %) (filtered-arenas))]
    (reverse (conj (shuffle (battle-filter (map arena->battle arenas))) (arena->battle main-arena)))))

;; This just calls one of the above two functions as appropriate and
;; then registers the battles in the session
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

;; I don't even remember what this is for
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

;; Minor battles are all the battles displayed after the "main" one at
;; the top
(defpartial _minor-battle [battle]
  (when battle
    (let [[left-f right-f] (:fighters battle)
          arena (:arena battle)]
      [:div.battle
       [:h2 (:fight-text (:arena battle))]
       [:div.fighter.a (card arena left-f "card")]
       [:div.fighter.b (card arena right-f "card")]])))

;; Divide the minor battles into rows so that they show up correctly
(defpartial _minor-battle-row [row]
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

;; This will display the main arena. Maybe it should be named main-arena
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

;; This is used to convert the data stored in a session for a battle
;; into something usable by the battle partial
(defn session-battle->battle-map [session-battle]
  (let [[prev-main-arena-shortname prev-fighter-id-a prev-fighter-id-b] session-battle]
    {:prev-main-arena-shortname prev-main-arena-shortname
     :prev-fighter-id-a prev-fighter-id-a
     :prev-fighter-id-b prev-fighter-id-b}))

;; The home page. Show completely random battles
(defpage-r listing []
  (battle (session-battle->battle-map (session/get :main-battle))))

;; When a user clicks on an image, determine which battle it's from so
;; that you can record the winner and show that battle's arena in the
;; main area
(defpage-r winner {:keys [arena-shortname _id]}
  (let [selected-battle ((session/get :battles) arena-shortname)
        selected-battle-fighter-ids (rest selected-battle)]
    (battle/record-winner! selected-battle-fighter-ids _id)
    (let [battle-map (session-battle->battle-map (or selected-battle (session/get :main-battle)))]
      (battle (assoc battle-map :main-arena-shortname (:prev-main-arena-shortname battle-map))))))

;; When you want to show a specific arena
(defpage-r arena {:keys [shortname]}
  (battle {:main-arena-shortname shortname}))