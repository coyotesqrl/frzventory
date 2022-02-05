(ns build
  (:refer-clojure :exclude [test])
  (:require [org.corfield.build :as bb]))

(def lib 'net.clojars.dreamloom/frzventory)
(def version "0.1.0-SNAPSHOT")
(def main 'dreamloom.frzventory)

(defn test "Run the tests." [opts]
  (bb/run-tests opts))

(defn static "Runs the static analyzers and linters." [opts]
  (-> opts
      (bb/run-task [:outdated])
      (bb/run-task [:eastwood])
      (bb/run-task [:kondo])
      (bb/run-task [:fmt-check])))

(defn ci "Run the CI pipeline of tests (and build the uberjar)." [opts]
  (-> opts
      (assoc :lib lib :version version :main main)
      static
      (bb/run-tests)
      (bb/clean)
      (bb/uber)))
