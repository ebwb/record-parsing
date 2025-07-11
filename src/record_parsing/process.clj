(ns record-parsing.process
  (:require [clojure.string :as s]
            [clojure.tools.logging :as log]
            [record-parsing.state :as db])
  (:import [java.time LocalDate]
           [java.time.format DateTimeFormatter]))

(def delimiters
  "Supported delimiters."
  {:pipe \|
   :comma \,
   :space \space})

(def datetime-fmt (DateTimeFormatter/ofPattern "M/d/uuuu"))

(defn detect-delimiter
  "Identify the delimiter based on the contents of the given line.
  Supported delimiters are specified in `record-parsing.core/delimiters`."
  [line]
  (let [delim-chars (vals delimiters)
        counts (frequencies (filter (set delim-chars) line))
        sorted (sort-by val > counts)]

    (when (or (empty? sorted)
              (and (> (count sorted) 1)
                   (= (val (second sorted)) (val (first sorted)))))
      (throw (ex-info "Ambiguous delimiter detected" {:counts counts})))

    ;; find the delimiter key that maps to the most frequent char
    (let [most-common-char (key (first sorted))]
      (some (fn [[k ch]] (when (= ch most-common-char) k)) delimiters))))

(defn ->record
  "Parse data into a record."
  [data]
  (try
    {:last-name (nth data 0)
     :first-name (nth data 1)
     :email (nth data 2)
     :favorite-color (nth data 3)
     :dob (LocalDate/parse (nth data 4) datetime-fmt)}
    (catch Exception e
      (log/error e)
      nil)))

(defn parse-record
  [line]
  (try
    (let [delimiter (detect-delimiter line)
          split-pattern (->> delimiter
                             (get delimiters)
                             (str "\\")
                             (re-pattern))]

      (log/debug "Delimiter used is" (name delimiter))
      (->record (s/split line split-pattern)))
    (catch Exception e
      (log/error e)
      nil)))

(defn serialize-dates
  "Serialize `java.time.LocalDate` fields into strings for JSON
  serialization."
  [records]
  (let [fmt-fn (fn [date] (str (.format date datetime-fmt)))]
    (cond
      (map? records)
      (update records :dob fmt-fn)

      (seq? records)
      (map #(update % :dob fmt-fn) records)

      :else
      [])))
  
(defn handle-add-record
  "Parse and add the record to the data store."
  [{:keys [body]}]
  (let [record (parse-record (slurp body))]
    (if record
      (do (db/add-> record)
          {:status 201 :body (serialize-dates record)})
      {:status 400 :body "Bad request"})))

(defn handle-get-records
  "Fetch and sort all records based on the given sort-fn."
  [sort-fn _]
  (->> (db/get-records)
       sort-fn
       serialize-dates))
