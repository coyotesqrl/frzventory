(ns dreamloom.frzventory
  (:gen-class)
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io])
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
  (sort (keys @freezer)))

(defn get-category [ctg]
  ((keyword ctg) @freezer))

(defn- update-item-count [name n v]
  (let [{item 'true items 'false} (group-by #(= (:name %) name) v)
        item (first item)
        item (if item (update item :count + n) {:name name :count n})]
    (if (< 0 (:count item))
      (conj items item)
      items)))

(defn add-item
  ([cat name] (add-item cat name 1))
  ([cat name n]
   (swap! freezer update (keyword cat) (partial update-item-count name n))))

(defn remove-item
  ([cat name] (remove-item cat name 1))
  ([cat name n]
   (swap! freezer update (keyword cat) (partial update-item-count name (- n)))))

(comment
  (load-freezer "data/freezer.edn")
  (get-category :beef)

  (add-item :beef "ribeye" 3)
  (add-item :beef "sirloin" 3)
  (add-item :beef "flank steak")
  (remove-item :beef "flank steak")
  (remove-item :beef "sirloin" 2)

  (let [{_ 'true others 'false} (->> (get-category :beef)
                                     (group-by #(= (:name %) "ribeye")))]
    (prn others))

  (->> "data/freezer.edn"
       io/reader
       (PushbackReader.)
       edn/read)

  (list-categories)
  (get-category :beef)

  (add-item :beef "ribeye" 3)

  (save-freezer "data/freezer.edn"))

(defn greet
  "Callable entry point to the application."
  [data]
  (println (str "Hello, " (or (:name data) "World") "!")))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (greet {:name (first args)}))
