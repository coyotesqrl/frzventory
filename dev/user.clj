(ns user
  (:require [portal.api :as p]
            [integrant.core :as ig]
            [tick.core :as t]
            [dreamloom.handler]
            [dreamloom.frzventory :as frz]
            [dreamloom.migrate :as migrate]))

(def migration-format (t/formatter "yyyyMMddHHmmss"))

(defn portal
  ([] (portal nil))
  ([l]
   (add-tap #'portal.api/submit)
   (p/open {:launcher l})))

(defn create-migration
  ([] (create-migration (t/instant)))
  ([timestamp]
   (let [timestamp (-> timestamp (t/in "UTC") (t/date-time))
         ns-name   (t/format migration-format timestamp)]
     (spit (str "src/dreamloom/migrations/" ns-name ".clj")
           (format "(ns dreamloom.migrations.%s%n\t(:require [xtdb.api :as xt]%n[dreamloom.xtdb :refer [node]]))", ns-name)))))

(def system (atom nil))

(defn init []
  (reset! system (ig/init (frz/system-config)))
  (migrate/run-migrations!))

(defn halt []
  (ig/halt! @system))
