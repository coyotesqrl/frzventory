(ns dreamloom.auth
  (:require [buddy.sign.jwt :as jwt]
            [buddy.hashers :as hash]
            [integrant.core :as ig]
            [tick.core :as t]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [ring.util.response :as response]
            [clojure.tools.logging :as log])
  (:import (java.io PushbackReader)
           (java.time Instant)))

(def users (->> "data/users.edn"
                io/reader
                (PushbackReader.)
                edn/read))

(defonce salt (atom nil))
(defonce jwt-secret (atom nil))

(defn- hash-value
  "Salts and encrypts the provided String password."
  [s]
  ; WARNING: Changing the algorithm, iterations count, or salt, or adding any other options to the
  ;          hash operation will cause mismatches between hashed values saved before the changes.
  ;          Insofar as these saved hashed values are used to determine whether a borrower's SSN has
  ;          changed since a prior credit report pull, an incorrect mismatch may trigger a re-pull
  ;          rather than a re-issue.
  (hash/derive s {:alg        :bcrypt+sha512
                  :iterations 12
                  :salt       @salt}))

(defn login [{:keys [form-params]}]
  (let [{:strs [username password]} form-params
        p-hash (hash-value password)
        jwt (when (= p-hash (get users (keyword username)))
              (jwt/sign {:user username
                         :expires-seconds  (t/>> (t/now) (t/of-hours 1))} @jwt-secret))]
    (if jwt
      (log/infof "Login success by %s" username)
      (log/warnf "Login failure by %s" username))
    (assoc-in (response/redirect "/") [:session :jwt] jwt)))

(defn logout []
  (assoc (response/redirect "/login")
         :session nil))

(defn validate [token]
  (when token
    (let [{:keys [user expires-seconds]} (jwt/unsign token @jwt-secret)]
      (when (< (.getEpochSecond ^Instant (t/now)) expires-seconds)
        {:user user}))))

(defmethod ig/init-key ::config
  [_ config]
  (reset! salt (slurp (:salt config)))
  (reset! jwt-secret (slurp (:jwt-secret config))))
