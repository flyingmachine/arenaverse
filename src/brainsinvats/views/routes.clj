(ns brainsinvats.views.routes)

(def r '{:arenas/shiny [shiny "/arenas/new"]
         :arenas/edit  [[:get "/arenas/edit/:id"]]})

(defmacro defpage-r [route & body]
  `(noir.core/defpage ~@((keyword (str (re-find #"[^.]*$" (str (ns-name *ns*))) "/" route)) r) ~@body))