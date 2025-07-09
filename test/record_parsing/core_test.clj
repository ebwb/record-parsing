(ns record-parsing.core-test
  (:require [clojure.test :refer :all]
            [record-parsing.core :as sut]
            [record-parsing.sort :as sorts]
            [clojure.java.io :as io])
  (:import [clojure.lang ExceptionInfo]))

(deftest test-parse-opts
  (testing "parse-opts tests"
    (with-redefs [sut/exit-with-err (fn [msg] msg)]
      (testing "happy path"
        (is (= {:sort sorts/sort->last-name-desc}
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
