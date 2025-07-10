(ns record-parsing.router
  (:require [reitit.ring :as ring]
            [reitit.ring.middleware.parameters :as parameters]
            [clojure.string :as s]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [record-parsing.process :as p]
            [record-parsing.sort :as sort])
  (:import [java.io ByteArrayInputStream InputStreamReader BufferedReader]))

(defn json-response
  [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/write-str data)})


(def router
  (ring/ring-handler
   (ring/router
    [["/records"
      {:post (fn [_] (json-response []))}]
     ["/records/color"
      {:get (fn [_] (json-response []))}]
     ["/records/birthdate"
      {:get (fn [_] (json-response []))}]
     ["/records/name"
      {:get (fn [_] (json-response []))}]])
   (ring/create-default-handler)))
