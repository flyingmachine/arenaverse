(ns arenaverse.server
  (:require [noir.server :as server]
            [monger.core :as mg]))

(server/load-views "src/arenaverse/views/")

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (let [uri (get (System/getenv) "MONGOLAB_URI" (str "mongodb://127.0.0.1/omgsmackdown-" (name mode)))]
      (monger.core/connect-via-uri! uri))
    (server/start port {:mode mode
                        :ns 'arenaverse})))