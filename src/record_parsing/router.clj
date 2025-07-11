(ns record-parsing.router
  (:require [reitit.ring :as ring]
            [clojure.data.json :as json]
            [record-parsing.process :as p]
            [record-parsing.sort :as sorts]))

;; On the next middleware addition, it will likely be time to abstract
;; these into their own namespace
(defn wrap-json-response
  "Middleware for turning body into JSON. Endpoints may return either
  data to be serialized or a map containing the intended `:status` and
  `:body` to be serialized."
  [handler]
  (fn [request]
    (let [resp (handler request)]
      (if (and (map? resp)
               (contains? resp :status)
               (contains? resp :body))
        (assoc resp
               :body (json/write-str (:body resp))
               :headers {"Content-Type" "application/json"})
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
         :body {:error "Unsupported Content-Type. Expected text/plain."}}))))

(def router
  (ring/ring-handler
   (ring/router
    [
     ["" {:middleware [wrap-json-response]}
      ["/records"
       {:post {:middleware [require-text-plain]
               :handler p/handle-add-record}}]
      ["/records/color"
       {:get (partial #'p/handle-get-records sorts/by-color-asc-last-name-asc)}]
       ["/records/birthdate"
        {:get (partial #'p/handle-get-records sorts/by-birth-date-asc)}]
      ["/records/name"
       {:get (partial #'p/handle-get-records sorts/by-last-name-desc)}]]])
   (ring/create-default-handler)))
