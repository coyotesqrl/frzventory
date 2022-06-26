(ns dreamloom.xtdb
  (:require [xtdb.api :as xt]
            [integrant.core :as ig]
            [clojure.tools.logging :as log])
  (:import (java.io Closeable)))

(defonce node (atom nil))

(defmethod ig/init-key ::config
  [_ config]
  (log/info "Starting XTDB node")
  (reset! node (xt/start-node config)))

(defmethod ig/halt-key! ::config
  [_ _]
  (swap! node #(.close ^Closeable %)))
