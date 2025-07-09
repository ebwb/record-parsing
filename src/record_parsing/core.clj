(ns record-parsing.core
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log])
  (:import [java.time LocalDate]
           [java.time.format DateTimeFormatter])
  (:gen-class))

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

(defn valid-line?
  [line]
  (and (not (s/blank? line))
       (not (s/starts-with? line "#"))))

(defn record->display
  [r]
  (s/join ", "
          [(:last-name r)
           (:first-name r)
           (:email r)
           (:favorite-color r)
           (str (.format (:dob r) datetime-fmt))]))

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

(defn clean
  [v]
  (s/lower-case (str v)))

(defn sort->last-name-desc
  [records]
  (sort
   #(compare (clean (:last-name %2)) (clean (:last-name %1)))
   records))

(defn sort->birth-date-asc
  [records]
  (sort #(compare (:dob %1) (:dob %2)) records))

(defn sort->color-asc-last-name-asc
  [records]
  (let [comp-fn
        (fn [a b]
          (or (compare (clean (:favorite-color a))
                       (clean (:favorite-color b)))
              (and (= (clean (:favorite-color a))
                      (clean (:favorite-color b)))
                   (compare (clean (:last-name a))
                            (clean (:last-name b))))))]

    (sort comp-fn records)))

(def sorts
  {"last-name-desc" sort->last-name-desc
   "birth-date-asc" sort->birth-date-asc
   "color-asc-last-name-asc" sort->color-asc-last-name-asc})

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

(def options
  "Valid options and their valid values."
  {"sort" sorts})

(defn exit-with-err
  [msg]
  (binding [*out* *err*]
    (println msg)
    (System/exit 1)))

(defn parse-opts
  [opts]
  (let [parse-fn
        (fn [[k v]]
          (let [k (s/replace (str k) #"--" "")
                opt-vs (get options k)
                chosen-v (get opt-vs v)]
            (if (nil? chosen-v)
              (exit-with-err
               (str "Invalid value for option '" k "': '" v "'.\n"
                    "Allowable values: " (keys opt-vs)))
              {(keyword k) chosen-v})))]
    
    (apply merge (map parse-fn (partition 2 opts)))))

(defn validate-input
  [filepath]
  
  (let [file (io/as-file filepath)]
    (cond
      (not (.exists file))
      (exit-with-err (str "File " filepath " does not seem to exist."))

      (not (.isFile file))
      (exit-with-err (str "File " filepath " is a directory."))

      ;; empty file may be valid usage, so do not print error
      (= 0 (.length file))
      (exit-with-err ""))))

(defn -main
  [& args]

  (let [filepath (first args)
        opts (rest args)
        options (parse-opts opts)
        default-sort (get sorts "last-name-desc")]
    
    ;; check file attributes before proceeding
    (validate-input filepath)
    
    (with-open [r (io/reader filepath)]
      ;; processing is wrapped around doall so file is processed
      ;; before we close the stream, but still allow for a lazy seq to
      ;; be passed around in case of a larger file.
      (doall
       (map #(println (record->display %))
            (process-data (line-seq r) (:sort options default-sort)))))))
