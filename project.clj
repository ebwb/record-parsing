(defproject record-parsing "0.0.0"
  :description "A small utility app for parsing records in a variety of formats"
  :url "https://github.com/ebwb/record-parsing/tree/develop"

  :dependencies [[org.clojure/clojure "1.12.1"]

                 ; logging
                 [org.clojure/tools.logging "1.3.0"]
                 [org.slf4j/slf4j-api "2.0.17"]
                 [org.slf4j/log4j-over-slf4j "2.0.17"]
                 [ch.qos.logback/logback-core "1.5.12"]
                 [ch.qos.logback/logback-classic "1.5.12"]]

  :main record-parsing.core

  :repl-options {:init-ns record-parsing.core}

  :profiles {:test [{:resource-paths ["test/resources"]}]}
)
