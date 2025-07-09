(ns record-parsing.sort
  (:require [clojure.string :as s]))

(defn clean
  [v]
  (s/lower-case (str v)))

(defn sort->last-name-desc
  [records]
  (sort
   #(compare (clean (:last-name %2)) (clean (:last-name %1)))
   records))

(defn sort->birth-date-asc
  [records]
  (sort #(compare (:dob %1) (:dob %2)) records))

(defn sort->color-asc-last-name-asc
  [records]
  (let [comp-fn
        (fn [a b]
          (or (compare (clean (:favorite-color a))
                       (clean (:favorite-color b)))
              (and (= (clean (:favorite-color a))
                      (clean (:favorite-color b)))
                   (compare (clean (:last-name a))
                            (clean (:last-name b))))))]

    (sort comp-fn records)))

(def sorts
  {"last-name-desc" sort->last-name-desc
   "birth-date-asc" sort->birth-date-asc
   "color-asc-last-name-asc" sort->color-asc-last-name-asc})
