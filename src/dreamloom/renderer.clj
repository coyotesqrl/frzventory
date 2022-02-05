(ns dreamloom.renderer
  (:require [selmer.parser :as sp]
            [dreamloom.frzventory :as f]))

(defn list-categories []
  (sp/render-file "selmer/list.html" {:items (f/list-categories)}))

(defn category->items [ctg]
  (sp/render-file "selmer/category-list.html" {:ctg ctg :items (f/get-category ctg)}))

(comment
  (f/get-category "beef"))