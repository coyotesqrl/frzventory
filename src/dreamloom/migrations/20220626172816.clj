(ns dreamloom.migrations.20220626172816
  (:require [xtdb.api :as xt]
            [dreamloom.xtdb :refer [node]]
            [dreamloom.core :as core]
            [clojure.string :as str]))

(defn- ctg->puts [ctg]
  (let [items (core/get-category ctg)
        ctg (str/lower-case ctg)
        item-fn (fn [i] (str/lower-case (:name i)))]
    (->> items
         (map #(vector ::xt/put {:xt/id    {:category ctg :item (item-fn %)}
                                 :category ctg
                                 :item     (item-fn %)
                                 :count    (:count %)})))))

(defn- build-txact []
  (->> (core/list-categories)
       (map :name)
       (mapcat ctg->puts)
       (vec)))

(xt/submit-tx @node (build-txact))
