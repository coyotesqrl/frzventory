(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.string :as str]
            [clojure.tools.build.api :as b]
            [clojure.tools.deps :as t]))

(def lib 'net.clojars.dreamloom/frzventory)
(def version "0.2.0-SNAPSHOT")
(def main 'dreamloom.frzventory)
(def class-dir "target/classes")

(defn- uber-opts [opts]
  (assoc opts
         :lib lib
         :main main
         :uber-file (format "target/%s-standalone.jar" lib)
         :basis (b/create-basis {})
         :class-dir class-dir
         :src-dirs ["src"]
         :ns-compile [main]))

(defn- run-task [aliases]
  (println "\nRunning task for" (str/join "," (map name aliases)))
  (let [basis    (b/create-basis {:aliases aliases})
        combined (t/combine-aliases basis aliases)
        cmds     (b/java-command
                  {:basis basis
                   :java-opts (:jvm-opts combined)
                   :main      'clojure.main
                   :main-args (:main-opts combined)})
        {:keys [exit]} (b/process cmds)]
    (when-not (zero? exit) (throw (ex-info "Task failed" {})))))

(defn test
  "Run the tests."
  [{:keys [aliases] :as opts}]
  (run-task (into [:test :dev] aliases))
  opts)

(defn cljfmt-check
  "Checks that code is formatted correctly"
  [opts]
  (run-task [:cljfmt-check])
  opts)

(defn cljfmt-fix
  "Fix code formatted incorrectly"
  [opts]
  (run-task [:cljfmt-fix])
  opts)

(defn lint
  "Run clj-kondo linter."
  [opts]
  (run-task [:clj-kondo])
  opts)

(defn outdated
  "Run the antq dependency checker."
  [opts]
  (run-task [:outdated])
  opts)

(defn ci
  "Run the CI pipeline: lint and test."
  [opts]
  (-> opts
      (assoc :lib lib :main main)
      cljfmt-check
      lint
      test))

(defn package
  "Generate uberjar for application."
  [opts]
  (b/delete {:path "target"})
  (let [opts (uber-opts opts)]
    (println "\nCopying source...")
    (b/copy-dir {:src-dirs ["resources" "src"] :target-dir class-dir})
    (println (str "\nCompiling " main "..."))
    (b/compile-clj opts)
    (println "\nBuilding JAR...")
    (b/uber opts))
  opts)
