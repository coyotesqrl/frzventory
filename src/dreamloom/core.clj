(ns dreamloom.core
  (:require [selmer.parser :as sp]
            [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:import (java.io PushbackReader)))

(def freezer (atom {}))

(def db "data/freezer.edn")

(defn load-freezer
  ([] (load-freezer db))
  ([f]
   (->> f
        io/reader
        (PushbackReader.)
        edn/read
        (reset! freezer))))

(load-freezer "data/freezer.edn")

(defn save-freezer
  ([] (save-freezer db))
  ([f]
   (spit f @freezer)
   "Saved"))

(defn list-categories []
  (->> (for [[k v] @freezer
             :let [v (apply + (map :count v))]]
         {:name k :count v})
       (sort-by :name)))

(defn get-category [ctg]
  (sort-by :name (get @freezer ctg)))

(defn- update-item-count [name n v]
  (let [{item 'true items 'false} (group-by #(= (:name %) name) v)
        item (first item)
        item (if item (update item :count + n) {:name name :count n})]
    (if (< 0 (:count item))
      (conj items item)
      items)))

(defn add-empty-category [ctg]
  (swap! freezer assoc ctg []))

(defn remove-empty-category [ctg]
  (if (empty? (get @freezer ctg))
    (swap! freezer dissoc ctg)
    @freezer))

(defn add-item
  ([ctg name] (add-item ctg name 1))
  ([ctg name n]
   (swap! freezer update ctg (partial update-item-count name n))))

(defn remove-item
  ([ctg name] (remove-item ctg name 1))
  ([ctg name n]
   (swap! freezer update ctg (partial update-item-count name (- n)))))

(defn login []
  (sp/render-file "selmer/login.html" {}))

(defn frz->list-categories []
  (sp/render-file "selmer/list.html" {:items (list-categories)}))

(defn category->items [ctg]
  (sp/render-file "selmer/category-list.html" {:ctg ctg :items (get-category ctg)}))
