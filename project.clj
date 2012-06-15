(defproject arenaverse "0.1.0-SNAPSHOT"
            :description "wooo!"
            :dependencies [[org.clojure/clojure "1.3.0"]
                           [noir "1.2.0" :exclusions [org.clojure/clojure]]
                           [com.novemberain/monger "1.0.0-rc1"]]
            :main arenaverse.server)

