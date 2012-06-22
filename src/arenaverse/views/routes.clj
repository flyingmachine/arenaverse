(ns arenaverse.views.routes
  (require [clojure.string :as string]))

(defn- throwf [msg & args]
  (throw (Exception. (apply format msg args))))

(def routes '{:arenas/listing   "/arenas"
              :arenas/shiny     "/arenas/new"
              :arenas/show      [:get "/arenas/:_id" :_id #"\d"]
              :arenas/edit      "/arenas/:_id/edit"
              :arenas/update    [:post "/arenas/:_id"]
              :arenas/create    [:post "/arenas"]
              :fighters/create  [:post "/fighters"]
              :fighters/edit    "/fighters/:_id/edit"
              :fighters/update  [:post "/fighters/:_id"]
              :fighters/destroy [:post "/fighters/:_id/destroy"]})


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

(defmacro defpage-r [route & body]
  (let [ns-suffix# (re-find #"[^.]*$" (str (ns-name *ns*)))]
    `(noir.core/defpage ~(symbol (str ns-suffix# "-" route)) ~((keyword (str ns-suffix# "/" route)) routes) ~@body)))
