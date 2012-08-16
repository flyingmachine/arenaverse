(ns arenaverse.server
  (:require [noir.server :as server]
            [monger.core :as mg]
            [cemerick.friend :as friend]
            [arenaverse.data-mappers.user :as user]
            [cemerick.drawbridge :as drawbridge]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds]
                             [openid :as openid])
            [ring.middleware.params :as params]
            [ring.middleware.keyword-params :as keyword-params]
            [ring.middleware.nested-params :as nested-params]
            [ring.middleware.session :as session]
            [ring.middleware.basic-authentication :as basic]))

(server/load-views "src/arenaverse/views/")

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (let [uri (get (System/getenv) "MONGOLAB_URI" (str "mongodb://127.0.0.1/omgsmackdown-" (name mode)))]
      (monger.core/connect-via-uri! uri))
    (server/start port {:mode mode
                        :ns 'arenaverse})))

;; Remote Repl
(def drawbridge-handler
  (-> (cemerick.drawbridge/ring-handler)
      (keyword-params/wrap-keyword-params)
      (nested-params/wrap-nested-params)
      (params/wrap-params)
      (session/wrap-session)))

(defn authenticated? [name pass]
  (= [name pass] [(System/getenv "AUTH_USER") (System/getenv "AUTH_PASS")]))

(defn wrap-drawbridge [handler]
  (fn [req]
    (let [handler (if (= "/repl" (:uri req))
                    (basic/wrap-basic-authentication
                     drawbridge-handler authenticated?)
                    handler)]
      (handler req))))

(server/add-middleware wrap-drawbridge)

;; Authorization
(defn credential-fn [username]
  (user/one {:username username}))

(defn admin-authorize [handler]
  (fn [request]
    (if (re-find #"/admin|/moderate" (:uri request))
      (friend/authorize #{"user"}
                        (handler request))
      (handler request))))

(server/add-middleware admin-authorize)

(server/add-middleware
      friend/authenticate 
      {:credential-fn (partial creds/bcrypt-credential-fn credential-fn)
       :workflows [(workflows/interactive-form), arenaverse.views.users/register]
       :login-uri "/login"
       :unauthorized-redirect-uri "/login" 
       :default-landing-uri "/admin"})
