(ns user
  (:require [portal.api :as p]
            [dreamloom.handler :as h]))

(defn portal
  ([] (portal nil))
  ([l]
   (add-tap #'portal.api/submit)
   (p/open {:launcher l})))

(defn run-app [] (h/run-app))

(defn stop-app [] (h/stop-app))
