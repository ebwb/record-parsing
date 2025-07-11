(ns record-parsing.main
  (:require [record-parsing.router :as router]
            [ring.adapter.jetty :as jetty]
            [clojure.tools.logging :as log])
  (:gen-class))

(def port 3000)

(defn -main
  [& _]
  (log/info "Starting server on port 3000.")
  (jetty/run-jetty router/router {:port port}))
