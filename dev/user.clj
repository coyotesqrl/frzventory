(ns user
  (:require [portal.api :as p]
            [integrant.core :as ig]
            [dreamloom.handler]
            [dreamloom.frzventory :as frz]))

(defn portal
  ([] (portal nil))
  ([l]
   (add-tap #'portal.api/submit)
   (p/open {:launcher l})))

(def system (atom nil))

(defn init []
  (reset! system (ig/init (frz/system-config))))

(defn halt []
  (ig/halt! @system))
