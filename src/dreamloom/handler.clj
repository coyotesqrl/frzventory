(ns dreamloom.handler
  (:require [dreamloom.core :as core]
            [clojure.data.json :as json]
            [compojure.core :refer [defroutes GET POST PUT DELETE context]]
            [compojure.route :as route]
            [ring.adapter.jetty :as jt])
  (:import (org.eclipse.jetty.server Server)))

(defroutes app-routes
  (GET "/" []
    {:status  200
     :headers {"content-type" "application/json"}
     :body    (json/write-str (core/list-categories))})
  (GET "/:category" [category]
    {:status  200
     :headers {"content-type" "application/json"}
     :body    (json/write-str (core/get-category category))})
  (PUT "/:category/add" [category]
    {:status 200
     :headers {"content-type" "application/json"}
     :body (json/write-str (core/add-empty-category category))})
  (DELETE "/:category/delete-empty" [category]
    {:status  200
     :headers {"content-type" "application/json"}
     :body    (json/write-str (core/remove-empty-category category))})
  (PUT "/:category/:item/add" [category item]
    {:status 200
     :headers {"content-type" "application/json"}
     :body (json/write-str (core/add-item category item 1))})
  (PUT "/:category/:item/add/:cnt" [category item cnt]
    {:status 200
     :headers {"content-type" "application/json"}
     :body (json/write-str (core/add-item category item (Integer/parseInt cnt)))})
  (DELETE "/:category/:item/remove" [category item]
    {:status 200
     :headers {"content-type" "application/json"}
     :body (json/write-str (core/remove-item category item 1))})
  (DELETE "/:category/:item/remove/:cnt" [category item cnt]
    {:status 200
     :headers {"content-type" "application/json"}
     :body (json/write-str (core/remove-item category item (Integer/parseInt cnt)))})
  (POST "/save" []
    {:status 200
     :headers {"content-type" "application/json"}
     :body (json/write-str (core/save-freezer))})

  (route/not-found "<h1><a href=\"/\">go back</a></h1>"))

(def api-routes
  (context "/v1" _
    app-routes
    (fn [_]
      {:status 404 :headers {"content-type" "application/json"} :body {}})))

(defroutes selmer-routes
  (GET "/" []
    {:status 200
     :body (core/frz->list-categories)})
  (GET "/category/:category" [category]
    {:status 200
     :body (core/category->items category)}))

(defroutes all
  "Primary routes for the webserver."
  api-routes
  selmer-routes
  (route/resources "/")
  (route/not-found "<h1>Page not Found!</h1>"))

(defonce server (delay (jt/run-jetty #'all {:port 8080 :join? false})))

(defn run-app []
  (.start ^Server (deref server)))

(defn stop-app []
  (.stop ^Server (deref server)))
