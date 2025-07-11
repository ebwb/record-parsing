(ns record-parsing.router
  (:require [reitit.ring :as ring]
            [reitit.ring.middleware.parameters :as parameters]
            [clojure.string :as s]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [record-parsing.process :as p]
            [record-parsing.sort :as sorts])
  (:import [java.io ByteArrayInputStream InputStreamReader BufferedReader]))

(defn wrap-json-response
  [handler]
  (fn [request]
    (let [resp (handler request)]
      (if (and (map? resp)
               (contains? resp :status)
               (contains? resp :body))
        resp
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (json/write-str resp)}))))

(defn require-text-plain
  "Middleware that ensures the request has `text/plain` Content-Type"
  [handler]
  (fn [req]
    (let [content-type (get-in req [:headers "content-type"])]
      (if (= content-type "text/plain")
        (handler req)
        {:status 415
         :headers {"Content-Type" "application/json"}
         :body (json/write-str {:error "Unsupported Content-Type. Expected text/plain."})}))))

(def router
  (ring/ring-handler
   (ring/router
    ["" {:middleware [wrap-json-response]}
     ["/records"
      {:post {:middleware [require-text-plain]
              :handler (fn [_] [{:foo "records"}])}}]
     ["/records/color"
      {:get (fn [_] [{:foo "color"}])}]
     ["/records/birthdate"
      {:get (fn [_] [{:foo "birthdate"}])}]
     ["/records/name"
      {:get {:handler (fn [_] [{:foo "name"}])}}]]
    )
   (ring/create-default-handler)))
