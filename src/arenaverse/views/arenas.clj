(ns arenaverse.views.arenas
  (:require [arenaverse.views.common :as common]
            [arenaverse.views.admin.fighters :as fighters]
            [arenaverse.models.fighter :as fighter]
            [noir.session :as session]
            [monger.collection :as mc]))