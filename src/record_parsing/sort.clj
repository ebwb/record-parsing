(ns record-parsing.sort
  (:require [clojure.string :as s]))

(defn clean
  [v]
  (s/lower-case (str v)))

(defn by-last-name-desc
  [records]
  (sort
   #(compare (clean (:last-name %2)) (clean (:last-name %1)))
   records))

(defn by-birth-date-asc
  [records]
  (sort #(compare (:dob %1) (:dob %2)) records))

(defn by-color-asc-last-name-asc
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
