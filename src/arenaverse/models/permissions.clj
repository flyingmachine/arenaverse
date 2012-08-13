(ns arenaverse.models.permissions
  (:require [cemerick.friend :as friend]
            [arenaverse.data-mappers.arena :as arena]))

(defn current-user-id []
  (:_id (friend/current-authentication)))

(defn current-username []
  (:username (friend/current-authentication)))

(defn modify-arena? [arena]
  (= (:user-id arena) (current-user-id)))

(defn modify-fighter? [fighter]
  (modify-arena? (arena/one-by-id (:arena-id fighter))))

;; Pretty sure there's something in onlisp about this
(defmacro protect [check & body]
  `(if (not ~check)
     (noir.response/redirect (arenaverse.views.routes/url-for-r :battles/listing))
     (do ~@body)))


(def moderator-ids (clojure.string/split (get (System/getenv) "MODERATOR_NAMES" "daniel") #","))

(defn moderate? []
  (some #(= % (current-username)) moderator-ids))

(defn moderate-arenas? []
  (moderate?))

(defn moderate-arena? [arena]
  (moderate?))

(defn moderate-fighter? [fighter]
  (moderate?))