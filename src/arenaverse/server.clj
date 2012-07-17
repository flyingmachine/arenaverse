(ns arenaverse.server
  (:require [noir.server :as server]
            [monger.core :as mg]
            [cemerick.friend :as friend]
            [arenaverse.data-mappers.user :as user]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds]
                             [openid :as openid])))

(server/load-views "src/arenaverse/views/")

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (let [uri (get (System/getenv) "MONGOLAB_URI" (str "mongodb://127.0.0.1/omgsmackdown-" (name mode)))]
      (monger.core/connect-via-uri! uri))
    (server/start port {:mode mode
                        :ns 'arenaverse})))

(def users {"root" {:username "root"
                    :password (creds/hash-bcrypt "admin_password")
                    :roles #{::admin}}
            "jane" {:username "jane"
                    :password (creds/hash-bcrypt "user_password")
                    :roles #{::user}}})

(defn credential-fn [username]
  (user/one {:username username}))


(defn admin-authorize [handler]
  (fn [request]
    (if (re-find #"/admin" (:uri request))
      (friend/authorize #{"user"}
                        (handler request))
      (handler request))))

(server/add-middleware admin-authorize)

(server/add-middleware 
      friend/authenticate 
      {:credential-fn (partial creds/bcrypt-credential-fn credential-fn)
       :workflows [(workflows/interactive-form)] 
       :login-uri "/login" 
       :unauthorized-redirect-uri "/login" 
       :default-landing-uri "/"})