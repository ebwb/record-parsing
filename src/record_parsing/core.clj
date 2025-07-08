(ns record-parsing.core)

(def delimiters
  "Supported delimiters."
  {\| :pipe \, :comma \space :space})

(defn detect-delimiter
  "Identify the delimiter based on the contents of the given
  line. Supported delimiters are specified in
  `record-parsing.core/delimiters`"
  [line]
  ;; TODO(ebwb): currently, this scans every character in the given
  ;; line for each known delimiter. we can likely scan the line once
  ;; and arrive at the same answer.
  (let [counts (into {} (for [[char key] delimiters]
                          [key (count (filter #(= % char) line))]))
        sorted (sort-by val > counts)]

    ;; if the second-most prevalent delimiter appears as much as the
    ;; most prevalent delimiter, the line may not be delimited at all.
    (if (= (second (vals sorted)) (val (first sorted)))
      (throw (ex-info "Ambiguous delimiter detected" {:counts counts}))
      (key (first sorted)))))
