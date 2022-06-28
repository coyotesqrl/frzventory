(ns dreamloom.migrate
  (:require [xtdb.api :as xt]
            [dreamloom.xtdb :refer [node]]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.string :as str])
  (:import (java.util.jar JarEntry JarFile)
           (java.net URL URLDecoder)
           (java.io File)
           (java.util.regex Pattern)))

(def migrations-path "dreamloom/migrations/")
(def migrations-ns-root "dreamloom.migrations.")
(def migration-file-pattern #"^(\d{14})\.clj")

;; TODO Don't use eval for this
(defn- run-migration [n]
  (let [run-cmd (str "(" migrations-ns-root n "/run)")]
    (eval (read-string run-cmd))))

(defn jar-file [^URL url]
  (some-> url
          (.getFile)
          (URLDecoder/decode "UTF-8")
          (.split "!/")
          ^String (first)
          (.replaceFirst "file:" "")
          (JarFile.)))

(defn find-migration-dir
  "Finds the given directory on the classpath. For backward
  compatibility, tries the System ClassLoader first, but falls back to
  using the Context ClassLoader like Clojure's compiler.
  If classloaders return nothing try to find it on a filesystem."
  ([]
   (-> (Thread/currentThread)
       (.getContextClassLoader)
       (find-migration-dir migrations-path)))

  ([^ClassLoader class-loader ^String dir]
   (if-let [^URL url (.getResource class-loader dir)]
     (if (= "jar" (.getProtocol url))
       (jar-file url)
       (File. (URLDecoder/decode (.getFile url) "UTF-8")))
     (let [migration-dir (io/file dir)]
       (if (.exists migration-dir)
         migration-dir
         (let [no-implicit-parent-dir (io/file dir)]
           (when (.exists no-implicit-parent-dir)
             no-implicit-parent-dir)))))))

(defn find-migration-files [migration-dir]
  (log/info "Looking for migrations in" migration-dir)
  (->> (for [f (filter (fn [^File f] (.isFile f))
                       (file-seq migration-dir))
             :let [file-name (.getName ^File f)]]
         (second (re-matches migration-file-pattern file-name)))
       (remove nil?)))

(defn find-migration-resources [jar]
  (log/info "Looking for migrations in" migrations-path jar)
  (->> (for [entry (enumeration-seq (.entries ^JarFile jar))
             :when (.matches (.getName ^JarEntry entry)
                             (str "^" (Pattern/quote migrations-path) ".+"))
             :let [entry-name       (.replaceAll (.getName ^JarEntry entry) migrations-path "")
                   last-slash-index (str/last-index-of entry-name "/")
                   file-name        (subs entry-name (if-not last-slash-index
                                                       0
                                                       (+ 1 last-slash-index)))]]
         (second (re-matches migration-file-pattern file-name)))
       (remove nil?)))

(defn find-migrations []
  (let [migration-dir (find-migration-dir)]
    (if (instance? File migration-dir)
      (find-migration-files migration-dir)
      (find-migration-resources migration-dir))))

(defn- last-migration []
  (-> @node
      (xt/db)
      (xt/entity :migration)
      :ns-name))

(defn- last-migration! [ns-name]
  (xt/submit-tx @node [[::xt/put {:xt/id :migration :ns-name ns-name}]]))

(defn run-migrations! []
  (let [last-run        (last-migration)
        migrations      (->> (find-migrations)
                             (filter #(> (compare % last-run) 0))
                             (sort))
        final-migration (last migrations)]
    (doseq [migration migrations]
      (log/infof "Running migration %s" migration)
      (load (str "/" migrations-path migration))
      (run-migration migration))
    (when final-migration
      (last-migration! final-migration))
    (log/info "Migrations complete")))
