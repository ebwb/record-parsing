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

;; TODO(ebwb): should have some validation of some sort, most likely,
;; but it won't be this
(defn valid-line?
  [line]
  (and (not (s/blank? line))
       (not (s/starts-with? line "#"))))

(defn ->record
  "Parse data into a record."
  [data]
  {:last-name (nth data 0)
   :first-name (nth data 1)
   :email (nth data 2)
   :favorite-color (nth data 3)
   :dob (LocalDate/parse (nth data 4) datetime-fmt)})

(defn parse
  "Parse a line of data into a record. Assumes that pattern has been
  correctly identified for the given data."
  [pattern data]
  (-> data
      (s/split pattern)
      ->record))

;; TODO(ebwb): would be nice to keep this open to accepting a list of
;; inputs, splitting on new lines, maybe
(defn process-data
  "Process input file's valid lines into output."
  [lines sort-fn]
  ;; drop noise lines
  (let [data (filter valid-line? lines)
        delimiter (detect-delimiter (first data))
        split-pattern (->> delimiter
                           (get delimiters)
                           (str "\\")
                           (re-pattern))]
    (log/debug "Delimiter used is" (name delimiter))

    ;; parse and display data
    (->> data
         (map (partial parse split-pattern))
         (sort-fn))))

(defn process-record
  [line]
  (let [data (filter valid-line? line)
        delimiter (detect-delimiter (first data))
        split-pattern (->> delimiter
                           (get delimiters)
                           (str "\\")
                           (re-pattern))]
    (log/debug "Delimiter used is" (name delimiter))

    (->> (first data)
         (parse split-pattern))))

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
  
;; TODO(ebwb): input validation?
(defn handle-add-record
  [{:keys [body]}]
  ;; TODO(ebwb): get rid of the call to vec
  (let [record (process-record (vector (slurp body)))]
    (if record
      (do (db/add-> record)
          (serialize-dates record))
      {:status 400 :error "Bad request"})))

(defn handle-get-records
  [sort-fn _]
  (->> (db/get-records)
       sort-fn
       serialize-dates))
