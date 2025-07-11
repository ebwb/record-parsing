(ns record-parsing.state)

;; for now, we hold state of records in memory
(def record-state (atom []))

;; these functions are simple, but they allow us to easily swap in
;; another stateful strategy if we'd like, as well as make it easier
;; to stub the current state out in unit tests

(defn add->
  [record]
  (swap! record-state conj record))

(defn get-records
  []
  @record-state)
