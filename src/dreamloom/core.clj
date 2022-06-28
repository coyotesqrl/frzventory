(ns dreamloom.core
  (:require [selmer.parser :as sp]
            [xtdb.api :as xt]
            [dreamloom.xtdb :refer [node]]
            [clojure.string :as str]))

(defn list-categories []
  (xt/q (xt/db @node)
        '{:find     [?ctg
                     (ignore-blanks p)
                     (sum ?count)]
          :where    [[p :category ?ctg]
                     [p :count ?count]]
          :order-by [[?ctg :asc]]}))

(defn get-category [ctg]
  (xt/q (xt/db @node)
        '{:find     [?item ?count]
          :in       [?ctg]
          :where    [[p :item ?item]
                     (not [p :item :blank])
                     [p :count ?count]
                     [p :category ?ctg]]
          :order-by [[?item :asc]]}
        ctg))

(defn- manage-item
  "If the specified item exists, will update the count by `delta` (positive or negative).
  If the specified item does not exist, will add it to the inventory with a count of `delta`.
  If the count of item in the inventory becomes zero, will delete it from the inventory.
  If the caller passes in a `delta` of zero, this is a no-op."
  [ctg name delta]
  (when-not (zero? delta)
    (let [ctg  (str/lower-case ctg)
          name (str/lower-case name)]
      (xt/submit-tx @node [[::xt/fn :update-item-count {:category ctg :item name} delta]]))))

(defn add-empty-category [ctg]
  (let [ctg (str/lower-case ctg)]
    (xt/submit-tx @node [[::xt/put {:xt/id    {:category ctg :item :blank}
                                    :category ctg
                                    :item     :blank
                                    :count    0}]])))

(defn remove-empty-category [ctg]
  (xt/submit-tx @node [[::xt/delete {:category ctg :item :blank}]]))

(defn add-item
  ([ctg name] (add-item ctg name 1))
  ([ctg name n]
   (manage-item ctg name n)))

(defn remove-item
  ([ctg name] (remove-item ctg name 1))
  ([ctg name n]
   (manage-item ctg name (* -1 n))))

(defn login []
  (sp/render-file "selmer/login.html" {}))

(defn frz->list-categories []
  (sp/render-file "selmer/list.html" {:items (list-categories)}))

(defn category->items [ctg]
  (sp/render-file "selmer/category-list.html" {:ctg ctg :items (get-category ctg)}))
