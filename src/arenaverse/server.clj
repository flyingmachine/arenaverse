(ns arenaverse.server
  (:require [noir.server :as server]
            [monger.core :as mg]))

(server/load-views "src/arenaverse/views/")

(def env :dev)

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (def env mode)
    (mg/connect!)
    (mg/set-db! (mg/get-db (str "omgsmackdown-" env)))
    (server/start port {:mode mode
                        :ns 'arenaverse})))

