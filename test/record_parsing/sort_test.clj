(ns record-parsing.sort-test
  (:require [clojure.test :refer :all]
            [record-parsing.sort :as sut])
  (:import [java.time LocalDate]))

(deftest test-sorts
  (testing "test last-name-desc sort"
    (let [input [{:last-name "zzz"}
                 {:last-name "aaa"}
                 {:last-name "mmm"}
                 {:last-name "aab"}]]
      (is (= [{:last-name "zzz"}
	      {:last-name "mmm"}
	      {:last-name "aab"}
	      {:last-name "aaa"}]
             (sut/sort->last-name-desc input)))))
  
  (testing "test birth-date-asc sort"
    (let [d->str #(LocalDate/parse %)
          input [{:dob (d->str "2005-06-30")}
                 {:dob (d->str "2000-01-01")}
                 {:dob (d->str "2010-01-01")}
                 {:dob (d->str "2000-01-02")}]]
      (is (= [{:dob (d->str "2000-01-01")}
              {:dob (d->str "2000-01-02")}
              {:dob (d->str "2005-06-30")}
              {:dob (d->str "2010-01-01")}]
             (sut/sort->birth-date-asc input)))))

  (testing "test color-asc-last-name-asc"
    (let [input [{:favorite-color "green" :last-name "SameAsOtherBBB"}
                 {:favorite-color "orange" :last-name "OnlyOrange"}
                 {:favorite-color "blue" :last-name "OnlyBlue"}
                 {:favorite-color "green" :last-name "SameAsOtherAAA"}]]

      (is (= [{:favorite-color "blue" :last-name "OnlyBlue"}
              {:favorite-color "green" :last-name "SameAsOtherBBB"}
              {:favorite-color "green" :last-name "SameAsOtherAAA"}
              {:favorite-color "orange" :last-name "OnlyOrange"}]
             (sut/sort->color-asc-last-name-asc input))))))

