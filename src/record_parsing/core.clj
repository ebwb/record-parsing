(ns record-parsing.core
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [record-parsing.sort :as sorts]
            [record-parsing.process :as p])
  (:gen-class))

(def options
  "Valid options and their valid values."
  {"sort" sorts/sorts})

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

;; TODO(ebwb): keep default sort of last-name-desc

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

;; (defn -main
;;   [& args]

;;   (let [filepath (first args)
;;         opts (rest args)
;;         options (parse-opts opts)
;;         default-sort (get sorts/sorts "last-name-desc")]
    
;;     ;; check file attributes before proceeding
;;     (validate-input filepath)
    
;;     (with-open [r (io/reader filepath)]
;;       ;; processing is wrapped around doall so file is processed
;;       ;; before we close the stream, but still allow for a lazy seq to
;;       ;; be passed around in case of a larger file.
;;       (doall
;;        (map #(println (p/record->display %))
;;             (p/process-data (line-seq r) (:sort options default-sort)))))))
