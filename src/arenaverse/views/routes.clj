(ns arenaverse.views.routes
  (require [clojure.string :as string]))

(defn- throwf [msg & args]
  (throw (Exception. (apply format msg args))))

(def routes '{:arenas/listing  "/arenas"
              :arenas/show     "/arenas/:id"
              :arenas/edit     "/arenas/:id/edit"
              :arenas/update   [:post "/arenas/:id"]
              :arenas/shiny    "/arenas/new"
              :arenas/create   [:post "/arenas"]})

(defn url-for-r
  ([route-name] (url-for-r route-name {}))
  ([route-name route-args]     
     (let [entry (route-name routes)
           route (or (last (flatten entry)) entry)
           route-arg-names (noir.core/route-arguments route)]
       (when (nil? route)
         (throwf "missing route for %s" route-name))
       (when (not (every? #(contains? route-args %) route-arg-names))
         (throwf "missing route-arg for %s" [route-args route-arg-names]))
       (reduce (fn [path [k v]]
                 (assert (keyword? k))
                 (string/replace path (str k) (str v))) route route-args))))

(defmacro defpage-r [route & body]
  `(noir.core/defpage ~route ~((keyword (str (re-find #"[^.]*$" (str (ns-name *ns*))) "/" route)) routes) ~@body))
