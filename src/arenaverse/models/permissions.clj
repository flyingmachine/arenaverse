(ns arenaverse.models.permissions
  (:require [cemerick.friend :as friend]
            [arenaverse.data-mappers.arena :as arena]))

(defn current-user-id []
  (:_id (friend/current-authentication)))

(defn modify_arena? [arena]
  (= (:user-id arena) (current-user-id)))

(defn modify_fighter? [fighter]
  (modify_arena? (arena/one-by-id (:arena-id fighter))))

;; Pretty sure there's something in onlisp about this
(defmacro protect [check & body]
  `(if (not ~check)
     (noir.response/redirect (arenaverse.views.routes/url-for-r :battles/listing))
     (do ~@body)))