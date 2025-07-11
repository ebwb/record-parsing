(ns record-parsing.router-test
  (:require [clojure.test :refer [deftest testing is]]
            [ring.mock.request :as mock]
            [clojure.data.json :as json]
            [record-parsing.router :as sut]))

(deftest post-record-test
  (testing "POST /records"
    (let [res (sut/router (-> (mock/request :post "/records")
                              (mock/header "Content-Type" "text/plain")
                              (mock/body "Foo|Bar|bar.foo@example.com|blue|2/14/1992")))
          body (json/read-str (:body res) {:key-fn keyword})]
      (is (= 200 (:status res)))
      (is (= "application/json" (get-in res [:headers "Content-Type"])))
      (is (= [{:foo "records"}] body))))
  (testing "POST /records with wrong content type returns 415"
    (let [res (sut/router (-> (mock/request :post "/records")
                              (mock/header "Content-Type" "application/json")
                              (mock/body "{\"foo\": 123}")))
          body (json/read-str (:body res) {:key-fn keyword})]
      (is (= 415 (:status res)))
      (is (= "application/json" (get-in res [:headers "Content-Type"])))
      (is (= {:error "Unsupported Content-Type. Expected text/plain."}
             body)))))

(deftest get-records-test
  (testing "GET /records/color"
    (let [res (sut/router (mock/request :get "/records/color"))
          body (json/read-str (:body res) {:key-fn keyword})]
      (is (= 200 (:status res)))
      (is (= "application/json" (get-in res [:headers "Content-Type"])))
      (is (= [{:foo "color"}] body))))

  (testing "GET /records/birthdate"
    (let [res (sut/router (mock/request :get "/records/birthdate"))
          body (json/read-str (:body res) {:key-fn keyword})]
      (is (= 200 (:status res)))
      (is (= "application/json" (get-in res [:headers "Content-Type"])))
      (is (= [{:foo "birthdate"}] body))))

  (testing "GET /records/name"
    (let [res (sut/router (mock/request :get "/records/name"))
          body (json/read-str (:body res) {:key-fn keyword})]
      (is (= 200 (:status res)))
      (is (= "application/json" (get-in res [:headers "Content-Type"])))
      (is (= [{:foo "name"}] body)))))
