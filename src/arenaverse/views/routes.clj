(ns arenaverse.views.routes
  (require [clojure.string :as string]))

(defn- throwf [msg & args]
  (throw (Exception. (apply format msg args))))

(def routes '{;; admin
              :admin/arenas/listing   "/admin"
              :admin/arenas/shiny     "/admin/arenas/new"
              :admin/arenas/show      [:get ["/admin/arenas/:_id" :_id #"((?!new)[^/])*"]]
              :admin/arenas/edit      "/admin/arenas/:_id/edit"
              :admin/arenas/update    [:post "/admin/arenas/:_id"]
              :admin/arenas/create    [:post "/admin/arenas"]
              :admin/arenas/destroy   [:post "/admin/arenas/:_id/destroy"]
              
              :admin/fighters/create  [:post "/admin/fighters"]
              :admin/fighters/edit    "/admin/fighters/:_id/edit"
              :admin/fighters/update  [:post "/admin/fighters/:_id"]
              :admin/fighters/destroy [:post "/admin/fighters/:_id/destroy"]

              :battles/listing        "/"
              :battles/winner         "/winner/:_id"})


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
