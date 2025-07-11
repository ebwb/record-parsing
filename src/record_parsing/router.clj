(ns record-parsing.router
  (:require [reitit.ring :as ring]
            [reitit.ring.middleware.parameters :as parameters]
            [clojure.string :as s]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [record-parsing.process :as p])
  (:import [java.io ByteArrayInputStream InputStreamReader BufferedReader]))

(defn json-response
  [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/write-str data)})

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
    [["/records"
      {:post {:middleware [require-text-plain]
              :handler (fn [_] (json-response []))}}]
     ["/records/color"
      {:get (fn [_] (json-response []))}]
     ["/records/birthdate"
      {:get (fn [_] (json-response []))}]
     ["/records/name"
      {:get (fn [_] (json-response []))}]])
   (ring/create-default-handler)))
