(ns dreamloom.frzventory
  (:gen-class)
  (:require [dreamloom.handler :as h]))

(defn -main
  [& args]
  (prn args)
  (h/run-app))
