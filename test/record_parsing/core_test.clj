(ns record-parsing.core-test
  (:require [clojure.test :refer :all]
            [record-parsing.core :as sut]
            [clojure.java.io :as io]
            [clojure.string :as s])
  (:import [clojure.lang ExceptionInfo]
           [java.time LocalDate]))


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
            :dob (LocalDate/parse "1/5/2025")}
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

(def comma-separated (-> "comma-delimited.txt"
                         io/resource
                         slurp
                         s/split-lines))

(deftest test-parse
  (testing "full parse test"
    (let [input "America,Andy,andy.america@yopmail.com,blue,2/2/1981"
          result (sut/parse #"," input)]
      (is (= {:last-name "America"
              :first-name "Andy"
	      :email "andy.america@yopmail.com"
	      :favorite-color "blue"
	      :dob (LocalDate/parse "1981-02-02")}
             result)))))

(deftest test-record->display
  (testing "asserting format of record->display"
    (let [input {:last-name "America"
                 :first-name "Andy"
	         :email "andy.america@yopmail.com"
	         :favorite-color "blue"
	         :dob (LocalDate/parse "1981-03-30")}
          result (sut/record->display input)]
      (is (= "America, Andy, andy.america@yopmail.com, blue, 3/30/1981"
             result)))))

(deftest test-parse-opts
  (testing "parse-opts tests"
    (with-redefs [sut/exit-with-err (fn [msg] msg)]
      (testing "happy path"
        (is (= {:sort sut/sort->last-name-desc}
               (sut/parse-opts ["--sort" "last-name-desc"]))))
      (testing "no options returns nil"
        (is (= nil (sut/parse-opts []))))
      (testing "providing an unknown value returns message"
        (is (= (str "Invalid value for option 'sort': 'foo'.\n"
                    "Allowable values: "
                    "(\"last-name-desc\" \"birth-date-asc\" \"color-asc-last-name-asc\")")
               (sut/parse-opts ["--sort" "foo"])))))))

(deftest test-valid-file?
  (testing "file existence and properties"
    (with-redefs [sut/exit-with-err (fn [msg] msg)]
      (testing "file is valid")
      ;; create a temp file for testing
      (let [temp-file (java.io.File/createTempFile "testfile" ".txt")]
        (spit temp-file "hello")
        (is (nil? (sut/validate-input (.getPath temp-file))))
        (.delete temp-file))

      (testing "non-existent file"
        (is (= "File /non/existent/file.txt does not seem to exist."
               (sut/validate-input "/non/existent/file.txt"))))

      (testing "directory instead of file"
        (let [dir (io/file ".")]
          (is (= "File . is a directory." (sut/validate-input (.getPath dir)))))))))
