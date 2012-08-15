(ns arenaverse.views.admin.fighters
  (:require [arenaverse.views.common :as common]
            [arenaverse.data-mappers.fighter :as fighter]
            [arenaverse.data-mappers.arena :as arena]
            [arenaverse.models.permissions :as permissions]
            [noir.response :as res]
            [noir.session :as session]
            [monger.collection :as mc]
            [noir.validation :as vali])
  
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers
        arenaverse.views.routes)

  (:import [org.bson.types ObjectId]))

(defn valid? [{:keys [name]}]
  (vali/rule (vali/has-value? name)
             [:name "You must enter a name"])
  (not (vali/errors? :name)))

(defpartial error-item [[first-error]]
  [:p.error first-error])

(defn redirect-to-arena [fighter]
  (let [arena (arena/one-by-id (:arena-id fighter))]
    (res/redirect (url-for-r :admin/arenas/show {:shortname (:shortname arena)}))))

(defn all-teams [fighters]
  (disj
   (apply sorted-set
          (map #(when (:team %) clojure.string/lower-case (:team %))
               fighters))
   nil ""))

(defpage-r create {:as fighter}
  (permissions/protect
   (permissions/modify-fighter? fighter)
   (fighter/create fighter)
   (redirect-to-arena fighter)))

(defpartial fighter-img [version record]
  [:img {:src (if (:image-extension record) (fighter/amazon-image-path version record) "/img/_ui/broken.png")}])

(defpartial team-selection [selected team]
  [:li [:label (radio-button {} "team" (= selected team) team) team]])

(defpartial team-selections [selected teams]
  (map (partial team-selection selected) teams))

(defpartial fighter-fields [record arena-id]
  [:p "Please don't add anything NSFW (not safe for work) or NSFL (not safe for life)"]
  [:div.control-group
   (vali/on-error :name error-item)
   (label :name "Name (require)")
   [:div.controls (text-field :name (:name record))]]
  [:div.control-group
   (label :bio "Team")
   [:span.help "Fighters on the same team don't face each other. If there are no teams, all fighters face each other."]
   [:div.controls
    [:ul
     (team-selections (:team record) (all-teams (fighter/all {:arena-id arena-id})))
     [:li (text-field {:placeholder "New team"} :new-team)]]]]
  [:div.control-group
   (label :file "Pic")
   [:span.help "Fighters without a pic won't show up"]
   [:div.controls
    (file-upload :file)
    [:br]
    (if (:_id record)
      (fighter-img "card" record))]])

(defpage-r edit {:keys [_id]}
  (let [fighter (fighter/one-by-id _id)]
    (permissions/protect
     (permissions/modify-fighter? fighter)
     (common/admin-layout
      [:h1 "Editing Fighter: " (:name fighter)]
      (form-to {:enctype "multipart/form-data"}
               [:post (url-for-r  :admin/fighters/update {:_id _id})]
               [:table
                (fighter-fields fighter (:arena-id fighter))
                [:div.form-actions (submit-button "Update Fighter")]])
      [:hr]
      (form-to [:post (url-for-r :admin/fighters/destroy {:_id _id})]
               (hidden-field :arena-id (:arena-id fighter))
               (submit-button "Delete Fighter"))))))

;; TODO possibly do nested route so arena id is present
(defpage-r update {:as fighter-input}
  (let [fighter-id (:_id fighter-input)
        fighter (fighter/one-by-id fighter-id)]
    (permissions/protect
     (permissions/modify-fighter? fighter)
     (let [record (fighter/update fighter-id (dissoc fighter-input :_id))]
       (session/flash-put! "Fighter updated!")
       (redirect-to-arena record)))))

(defpage-r destroy {:keys [_id arena-id]}
  (fighter/destroy _id)
  (redirect-to-arena {:arena-id arena-id}))

(defpartial thumb [record]
  [:div.fighter
   [:div.card
    [:div.name
     [:a {:href (url-for-r :admin/fighters/edit record)} (:name record)]]
    [:div.pic
     (fighter-img "card" record)]
    [:div.team (:team record)]]])

(defpartial thumbs [& [query-doc]]
  (map (fn thumb-row [records]
         (into [:div.row] (map thumb records)))
       (partition-all 4 (fighter/all query-doc))))
