(ns dreamloom.handler
  (:require [dreamloom.frzventory :as f]
            [dreamloom.renderer :as rnd]
            [clojure.data.json :as json]
            [compojure.core :refer [defroutes GET POST PUT DELETE context]]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [ring.adapter.jetty :as jt])
  (:import (org.eclipse.jetty.server Server)))

(defroutes app-routes
  (GET "/" []
    {:status  200
     :headers {"content-type" "application/json"}
     :body    (json/write-str (f/list-categories))})
  (GET "/:category" [category]
    {:status  200
     :headers {"content-type" "application/json"}
     :body    (json/write-str (f/get-category category))})
  (PUT "/:category/:item/add" [category item]
    {:status 200
     :headers {"content-type" "application/json"}
     :body (json/write-str (f/add-item category item 1))})
  (PUT "/:category/:item/add/:cnt" [category item cnt]
    {:status 200
     :headers {"content-type" "application/json"}
     :body (json/write-str (f/add-item category item (Integer/parseInt cnt)))})
  (DELETE "/:category/:item/remove" [category item]
    {:status 200
     :headers {"content-type" "application/json"}
     :body (json/write-str (f/remove-item category item 1))})
  (DELETE "/:category/:item/remove/:cnt" [category item cnt]
    {:status 200
     :headers {"content-type" "application/json"}
     :body (json/write-str (f/remove-item category item (Integer/parseInt cnt)))})
  (POST "/save" []
    {:status 200
     :headers {"content-type" "application/json"}
     :body (json/write-str (f/save-freezer))})

  (route/not-found "<h1><a href=\"/\">go back</a></h1>"))

(def api-routes
  (context "/v1" _
    app-routes
    (fn [_]
      {:status 404 :headers {"content-type" "application/json"} :body {}})))

(defroutes selmer-routes
  (GET "/list" []
    {:status 200
     :body (rnd/list-categories)})
  (GET "/category/:category" [category]
    {:status 200
     :body (rnd/category->items category)}))

(defroutes all
  "Primary routes for the webserver."
  (GET "/" [] {}
    (resp/header (resp/resource-response "index.html" {:root "public"})
                 "Content-Type"
                 "text/html; charset=UTF-8"))
  api-routes
  selmer-routes
  (route/resources "/")
  (route/not-found "<h1>Page not Found!</h1>"))

(defonce server (delay (jt/run-jetty #'all {:port 8080 :join? false})))

(defn run-app []
  (.start ^Server (deref server)))

(defn stop-app []
  (.stop ^Server (deref server)))
