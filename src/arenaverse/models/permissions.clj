(ns arenaverse.models.permissions
  (:require [cemerick.friend :as friend]
            [arenaverse.views.routes :as routes]
            [noir.response :as res]))

(defn current_user_id []
  (:_id (friend/current-authentication)))

(defn modify_arena? [arena]
  (= (:user_id arena) (current_user_id)))

(defmacro protect [[check & args] & body]
  (if (not (apply check args))
    (res/redirect (routes/url-for-r :battles/listing))))