(ns dreamloom.nrepl
  (:require
   [cider.nrepl :refer (cider-nrepl-handler)]
   [clojure.tools.logging :as log]
   [integrant.core :as ig]
   [nrepl.server :as nrepl]))

(defmethod ig/init-key ::config
  [_ {:keys [port bind]}]
  (do
    (log/info (str "Starting nREPL server on port " port))
    {:server (nrepl/start-server :port port
                                 :bind bind
                                 :handler cider-nrepl-handler)}))

(defmethod ig/halt-key! ::config
  [_ {:keys [server]}]
  (nrepl/stop-server server)
  (log/info "nREPL server stopped"))
