(ns dreamloom.migrations.20220626172816
  (:require [xtdb.api :as xt]
            [dreamloom.xtdb :refer [node]]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:import (java.io PushbackReader)))

;;
;; Migration reader support from flat file
;;
(def freezer (->> "data/freezer.edn"
                  io/reader
                  (PushbackReader.)
                  edn/read))

(defn- list-categories []
  (->> (for [[k v] freezer
             :let [v (apply + (map :count v))]]
         {:name k :count v})
       (sort-by :name)))

(defn- get-category [ctg]
  (sort-by :name (get freezer ctg)))

(defn- ctg->puts [ctg]
  (let [items (get-category ctg)
        ctg (str/lower-case ctg)
        item-fn (fn [i] (str/lower-case (:name i)))]
    (->> items
         (map #(vector ::xt/put {:xt/id    {:category ctg :item (item-fn %)}
                                 :category ctg
                                 :item     (item-fn %)
                                 :count    (:count %)})))))

(defn- build-txact []
  (->> (list-categories)
       (map :name)
       (mapcat ctg->puts)
       (vec)))

(defn run []
  ;; Migrate data from old flat file format
  (xt/submit-tx @node (build-txact))

  ;; Create Transaction Function for updating count
  (xt/submit-tx @node [[::xt/put {:xt/id :update-item-count
                                  :xt/fn '(fn [ctx eid delta]
                                            (let [db     (xtdb.api/db ctx)
                                                  entity (xtdb.api/entity db eid)]
                                              (cond
                                                (and entity (zero? (+ (:count entity) delta)))
                                                [[::xt/delete eid]]

                                                (some? entity)
                                                [[::xt/put (update entity :count #(+ delta %))]]

                                                :else
                                                [[::xt/put {:xt/id    eid
                                                            :category (:category eid)
                                                            :item     (:item eid)
                                                            :count    delta}]])))}]]))
