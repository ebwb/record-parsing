(ns record-parsing.core-test
  (:require [clojure.test :refer :all]
            [record-parsing.core :as sut])
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
        
           
