(defproject arenaverse "0.1.0-SNAPSHOT"
            :description "wooo!"
            :dependencies [[org.clojure/clojure "1.4.0"]
                           [noir "1.2.1" :exclusions [org.clojure/clojure]]
                           [com.novemberain/monger "1.1.2"]
                           [com.cemerick/friend "0.0.9"]
                           [clj-aws-s3 "0.3.1"]
                           [org.imgscalr/imgscalr-lib "4.2"]
                           [com.cemerick/drawbridge "0.0.3"]
                           [ring-basic-authentication "1.0.1"]]
            :main arenaverse.server)

