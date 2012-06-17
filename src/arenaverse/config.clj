(ns arenaverse.config)

(def ^:dynamic *aws-credentials* {:secret-key (System/getenv "AWS_SECRET")
                                  :access-key "AKIAIJU3CDKDGEIAGGYQ"})