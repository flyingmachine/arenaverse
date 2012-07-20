(ns arenaverse.models.permissions
  (:require [cemerick.friend :as friend]))

(defn current-user-id []
  (:_id (friend/current-authentication)))

(defn modify_arena? [arena]
  (println "ARENA" arena "USER" (current-user-id))
  (= (:user-id arena) (current-user-id)))

(defmacro protect [check & body]
  `(if (not ~check)
     (noir.response/redirect (arenaverse.views.routes/url-for-r :battles/listing))
     (do ~@body)))