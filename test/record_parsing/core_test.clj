(ns record-parsing.core-test
  (:require [clojure.test :refer :all]
            [record-parsing.core :as sut]
            [clojure.java.io :as io]
            [clojure.string :as s])
  (:import [clojure.lang ExceptionInfo]))


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
        (is (= {:counts {:pipe 0 :comma 0 :space 0}} (ex-data e)))))))))

(deftest test-valid-line?
  (testing "valid-line?"
    (is (true? (sut/valid-line? "Foo")))
    (is (true? (sut/valid-line? "123")))
    (is (true? (sut/valid-line? "$%^")))
    (is (false? (sut/valid-line? " ")))
    (is (false? (sut/valid-line? "")))
    (is (false? (sut/valid-line? nil)))
    (is (false? (sut/valid-line? "#")))
    (is (false? (sut/valid-line? "# ")))))
    
(deftest test-parse
  (testing "parse parses valid records"
    (is (= {:last-name "last"
            :first-name "first"
            :email "foo@bar.com"
            :favorite-color "green"
            :dob "1/5/2025"}
           (sut/parse
            #"\#"
            "last#first#foo@bar.com#green#1/5/2025"))))
  (testing "fails to parse lines that use wrong pattern"
    (try
      (sut/parse #"\," "last|first|foo@bar.com|green|1/5/2025")
      (catch Exception e
        (is java.lang.IndexOutOfBoundsException (type e)))))
  (testing "fails to parse lines that don't have enough fields"
    (try
      (sut/parse #"\," "last,first,foo@bar.com,green")
      (catch Exception e
        (is java.lang.IndexOutOfBoundsException (type e))))))
        

(def comma-separated (-> "comma-delimited.txt"
                         io/resource
                         slurp
                         s/split-lines))

(deftest test-parse
  (testing "full parse test"
    (let [result (sut/process comma-separated)]
      (is (= [{:last-name "Tirekicker"
	      :first-name "Ruth"
	      :email "ruth.tirekicker@yopmail.com"
	      :favorite-color "black"
	      :dob "2/7/1984"}
	     {:last-name "Homeowner"
	      :first-name "John"
	      :email "john.homeowner@yopmail.com"
	      :favorite-color "white"
	      :dob "1/1/1980"}]
             result)))))
