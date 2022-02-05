(ns fiddle1)

(def freezer (atom {}))

(defn list-categories []
  (->> @atom
       (map :category)
       distinct
       sort))

(defn get-category [cat]
  (->> @atom
       (filter #(= cat (:category %)))))

(defn add-item
  ([item] (add-item item 1))
  ([item n]))

(defn remove-item
  ([item] (remove-item item 1))
  ([item n]))

(comment
  ; Shape of item to add (date is tacked on when added)
  {:name "name" :category "category"}

  ; Shape of entry
  {:name "name"
   :category "category"
   :date "added-date"})

(comment
  "Selmer templates?")