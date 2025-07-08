(ns record-parsing.core
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]))

;; TODO(ebwb): namespace is getting unwieldy; may be ok for
;; command-line app, but will certainly need refactor before REST app
;; is complete

(def delimiters
  "Supported delimiters."
  {:pipe \|
   :comma \,
   :space \space})

(defn detect-delimiter
  "Identify the delimiter based on the contents of the given
  line. Supported delimiters are specified in
  `record-parsing.core/delimiters`"
  [line]
  ;; TODO(ebwb): currently, this scans every character in the given
  ;; line for each known delimiter. we can likely scan the line once
  ;; and arrive at the same answer.
  (let [counts (into {} (for [[key char] delimiters]
                          [key (count (filter #(= % char) line))]))
        sorted (sort-by val > counts)]

    ;; if the second-most prevalent delimiter appears as much as the
    ;; most prevalent delimiter, the line may not be delimited at all.
    (if (= (second (vals sorted)) (val (first sorted)))
      (throw (ex-info "Ambiguous delimiter detected" {:counts counts}))
      (key (first sorted)))))

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
   :dob (nth data 4)})

(defn parse
  "Parse a line of data into a record. Assumes that pattern has been
  correctly identified for the given data."
  [pattern data]
  (-> data
      (s/split pattern)
      ->record))

;; TODO(ebwb): could use a better name
(defn process
  "Process input file's valid lines into output."
  [lines]
  ;; drop noise lines
  (let [data (filter valid-line? lines)
        delimiter (detect-delimiter (first data))
        split-pattern (->> delimiter
                           (get delimiters)
                           (str "\\")
                           (re-pattern))]
    (log/info "Delimiter used is" (name delimiter))

    ;; parse and display data
    (map (partial parse split-pattern) data)))

(defn -main
  [filepath]

  ;; wrap around doall so file is processed before we close the
  ;; stream, but still allow for a lazy seq to be passed around in
  ;; case of a larger file.
  (with-open [r (io/reader filepath)]
    ;; TODO(ebwb): error handling, edge cases
    (println 
    (doall (process (line-seq r))))))
