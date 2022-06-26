(ns dreamloom.xtdb
  (:require [xtdb.api :as xt]
            [integrant.core :as ig]
            [clojure.tools.logging :as log])
  (:import (java.io Closeable)))

(defmethod ig/init-key ::config
  [_ config]
  (log/info "Starting XTDB node")
  (let [node (xt/start-node config)]
    node))

(defmethod ig/halt-key! ::config
  [_ ^Closeable node]
  (.close node))
