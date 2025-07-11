(ns record-parsing.process-test
  (:require [clojure.test :refer :all]
            [record-parsing.process :as sut]
            [clojure.java.io :as io]
            [clojure.string :as s])
  (:import [java.time LocalDate]))

(deftest test-detect-delimiter
  (testing "Lines with known delimiters are detected"
    (testing "comma-delimited lines"
      (is (= :comma (sut/detect-delimiter "foo,bar,baz@boo.com,1/1/1985"))))
    (testing "space-delimited lines"
      (is (= :space (sut/detect-delimiter "foo bar baz@boo.com 1/1/1985"))))
    (testing "pipe-delimited lines"
      (is (= :pipe (sut/detect-delimiter "foo|bar|baz@boo.com|1/1/1985")))

  (testing "Lines with unknown delimiters throw"
    (try
      (sut/detect-delimiter "foo%bar%baz@boo.com%quu%1/1/1985")
      (catch Exception e
        (is (= "Ambiguous delimiter detected" (.getMessage e)))
        (is (= {:counts {}} (ex-data e)))))))))

(deftest test-parse-record
  (testing "parse parses valid records"
    (is (= {:last-name "last"
            :first-name "first"
            :email "foo@bar.com"
            :favorite-color "green"
            :dob (LocalDate/parse "2025-01-05")}
           (sut/parse-record "last|first|foo@bar.com|green|1/5/2025"))))
  (testing "fails to parse lines that use unknown pattern"
    (is (nil? (sut/parse-record "last#first#foo@bar.com#green#1/5/2025"))))
  (testing "fails to parse lines that don't have enough fields"
    (is (nil? (sut/parse-record "last,first,foo@bar.com,green")))))

(deftest test-serialize-dates
  (testing "asserting format of serialize-dates"
    (let [input {:last-name "America"
                 :first-name "Andy"
	         :email "andy.america@yopmail.com"
	         :favorite-color "blue"
	         :dob (LocalDate/parse "1981-03-30")}
          result (sut/serialize-dates input)]
      (is (= (assoc input :dob "3/30/1981")
             result)))))
