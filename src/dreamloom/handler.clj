(ns dreamloom.handler
  (:require [dreamloom.core :as core]
            [dreamloom.auth :as auth]
            [dreamloom.middleware :as middleware]
            [ring.middleware.session :as session]
            [ring.middleware.session.memory :as memory]
            [clojure.data.json :as json]
            [compojure.core :refer [defroutes GET POST PUT DELETE context wrap-routes]]
            [compojure.route :as route]
            [ring.adapter.jetty :as jt])
  (:import (org.eclipse.jetty.server Server)))

(def session-atom (atom {}))

(defroutes app-route-def
  (GET "/" []
    {:status  200
     :headers {"content-type" "application/json"}
     :body    (json/write-str (core/list-categories))})
  (POST "/login" req (auth/login req))
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

(defroutes selmer-route-def
  (GET "/" []
    {:status 200
     :headers {"content-type" "text/html"}
     :body (core/frz->list-categories)})
  (GET "/login" []
    {:status 200
     :headers {"content-type" "text/html"}
     :body (core/login)})
  (GET "/category/:category" [category]
    {:status 200
     :headers {"content-type" "text/html"}
     :body (core/category->items category)}))

(def api-routes
  (wrap-routes
   (context "/v1" _
     app-route-def
     (fn [_]
       {:status 404 :headers {"content-type" "application/json"} :body {}}))
   (comp middleware/wrap-defaults
         #(session/wrap-session % {:store (memory/memory-store session-atom)})
         middleware/wrap-authenticate)))

(def selmer-routes
  (wrap-routes
   (context "/" _
     selmer-route-def
     (fn [_]
       {:status 404 :headers {"content-type" "text/html"} :body "Nope"}))
   (comp middleware/wrap-defaults
         #(session/wrap-session % {:store (memory/memory-store session-atom)})
         middleware/wrap-authenticate)))

(defroutes all
  "Primary routes for the webserver."
  (wrap-routes (route/resources "/res/") (comp session/wrap-session))
  api-routes
  selmer-routes
  (route/not-found "<h1>Page not Found!</h1>"))

(defonce server (delay (jt/run-jetty #'all {:port 8080 :join? false})))

(defn run-app []
  (.start ^Server (deref server)))

(defn stop-app []
  (.stop ^Server (deref server)))
