(ns arenaverse.views.routes
  (require [clojure.string :as string]))

(defn- throwf [msg & args]
  (throw (Exception. (apply format msg args))))

(def routes '{;; admin
              :admin/arenas/listing   "/admin"
              :admin/arenas/shiny     "/admin/arenas/new"
              :admin/arenas/show      [:get ["/admin/arenas/:shortname" :shortname #"((?!new)[^/])*"]]
              :admin/arenas/edit      "/admin/arenas/:shortname/edit"
              :admin/arenas/update    [:post "/admin/arenas/:shortname"]
              :admin/arenas/create    [:post "/admin/arenas"]
              :admin/arenas/destroy   [:post "/admin/arenas/:shortname/destroy"]
              
              :admin/fighters/create  [:post "/admin/fighters"]
              :admin/fighters/edit    "/admin/fighters/:_id/edit"
              :admin/fighters/update  [:post "/admin/fighters/:_id"]
              :admin/fighters/destroy [:post "/admin/fighters/:_id/destroy"]

              ;; moderate
              :moderate/arenas/listing "/moderate"
              :moderate/arenas/show    [:get ["/moderate/arenas/:shortname" :shortname #"((?!new)[^/])*"]]
              :moderate/arenas/hide    "/moderate/arenas/:shortname/hide"
              :moderate/arenas/unhide  "/moderate/arenas/:shortname/unhide"

              :moderate/fighters/hide    "/moderate/fighters/:_id/hide"
              :moderate/fighters/unhide  "/moderate/fighters/:_id/unhide"
              
              ;; battles
              :battles/listing        "/"
              :battles/winner         "/arenas/:arena-shortname/winner/:_id"
              :battles/arena          "/arenas/:shortname"

              ;; sessions
              :sessions/new           "/login"
              :sessions/destroy       "/logout"

              ;; users
              :users/shiny            "/signup"
              :users/create           [:post "/users"]}
  )


;; TODO handle symbols vs keywords?
(defn url-for-r
  ([route-name] (url-for-r route-name {}))
  ([route-name route-args]     
     (let [entry (route-name routes)
           route  (or (first (filter string? (flatten entry))) entry)
           route-arg-names (noir.core/route-arguments route)]
       (when (nil? route)
         (throwf "missing route for %s" route-name))
       (when (not (every? #(contains? route-args %) route-arg-names))
         (throwf "missing route-arg for %s" [route-args route-arg-names]))
       (reduce (fn [path [k v]]
                 (assert (keyword? k))
                 (string/replace path (str k) (str v))) route route-args))))

(defn- view-ns [namespace]
  ((re-find #"views\.(.*)$" (str (ns-name namespace))) 1))

(defn- dashed [namespace]
  (string/replace namespace "." "-")) ()

(defn- slashed [namespace]
  (string/replace namespace "." "/"))

(defmacro defpage-r [route & body]
  (let [ns-suffix# (view-ns *ns*)]
    `(noir.core/defpage ~(symbol (str (dashed ns-suffix#) "-" route)) ~((keyword (str (slashed ns-suffix#) "/" route)) routes) ~@body)))

