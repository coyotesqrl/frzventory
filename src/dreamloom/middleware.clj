(ns dreamloom.middleware
  (:require [ring.middleware.defaults :as defaults]
            [ring.util.response :as response]
            [dreamloom.auth :as auth]
            [clojure.string :as str]))

(defn wrap-defaults [handler]
  (defaults/wrap-defaults
   handler
   defaults/api-defaults
    ;; Need some sort of in-dev/not-in-dev test once HTTPS is configured
   #_(if cfg-core/in-kubernetes?
       (miss/deep-merge defaults/secure-api-defaults
                        {:proxy    true
                         :security {:ssl-redirect false}})
       defaults/api-defaults)))

(defn- logged-in? [req]
  (tap> {:logged-in? (auth/validate (get-in req [:session :jwt]))})
  (auth/validate (get-in req [:session :jwt])))

(defn wrap-authenticate
  ([handler] (wrap-authenticate handler {}))
  ([handler _]
   (fn [{:keys [uri] :as req}]
     (tap> {:req req :uri uri :session (:session req)})
     (if (or (logged-in? req)
             (str/starts-with? uri "/login")
             (str/starts-with? uri "/v1/login"))
       (handler req)
       (response/redirect "/login")))))
