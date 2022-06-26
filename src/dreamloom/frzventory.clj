(ns dreamloom.frzventory
  (:gen-class)
  (:require [clojure.java.io :as io]
            [aero.core :as aero]
            [integrant.core :as ig]
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

(defn -main
  [& args]
  (log/info args)
  (ig/init (system-config {:profile :prod})))
