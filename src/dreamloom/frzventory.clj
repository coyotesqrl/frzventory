(ns dreamloom.frzventory
  (:gen-class)
  (:require [clojure.java.io :as io]
            [aero.core :as aero]
            [integrant.core :as ig]
            [dreamloom.migrate :as migrate]
            [dreamloom.handler]
            [clojure.tools.logging :as log]))

(defmethod aero/reader 'ig/ref
  [_ _ value]
  (ig/ref value))

(defmethod ig/init-key ::secrets
  [_ _])

(defn system-config
  ([] (system-config nil))
  ([opts]
   (aero/read-config (io/resource "config.edn") opts)))

;; TODO cleaner command line passing
(defn -main
  [& args]
  (log/info args)
  (ig/init (system-config {:profile (or (keyword (first args)) :prod)}))
  (migrate/run-migrations!))
