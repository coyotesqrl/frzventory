(ns dreamloom.xtdb
  (:require [xtdb.api :as xt]
            [xtdb.query :as xq]
            [integrant.core :as ig]
            [clojure.tools.logging :as log])
  (:import (java.io Closeable)))

(defmethod xq/aggregate 'ignore-blanks [_]
  (fn aggregate-count
    (^long [] 0)
    (^long [^long acc] acc)
    (^long [^long acc {:keys [item]}] (if (= :blank item)
                                        acc
                                        (inc acc)))))

(defonce node (atom nil))

(defmethod ig/init-key ::config
  [_ config]
  (log/info "Starting XTDB node")
  (reset! node (xt/start-node config)))

(defmethod ig/halt-key! ::config
  [_ _]
  (swap! node #(.close ^Closeable %)))
