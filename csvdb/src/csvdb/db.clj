(ns csvdb.db
  "Namespace for database-related data & functions..."
  (:require [clojure.data.csv :as csv]
            [clojure.string :as str])
  (:use csvdb.helpers))

;; helper functions

(defn table-keys [tbl]
  (mapv keyword (first tbl)))

(defn key-value-pairs [tbl-keys tbl-record]
  (interleave tbl-keys tbl-record))

(def data-record zipmap)

(defn data-table [tbl]
  (map (partial data-record (table-keys tbl)) (next tbl)))

(defn str-field-to-int [field rec]
  (update-in rec [field] parse-int))

;; Place for saving data
;; use agent for concurrent access
(def student (agent ()))
(def subject (agent ()))
(def student-subject (agent ()))

(defn get-table [^String tb-name]
  "return mutable object use its name"
  (let [lname (str/lower-case tb-name)]
    (cond
      (= lname "student") student
      (= lname "subject") subject
      (= lname "student-subject") student-subject
      )))

(defn load-initial-data []
  "Load initial data from files .csv and save its in mutable variables student, subject, student-subject"
  (send student concat (->> (data-table (csv/read-csv (slurp "student.csv")))
                            (map #(str-field-to-int :id %))
                            (map #(str-field-to-int :year %))))
  (send subject concat (->> (data-table (csv/read-csv (slurp "subject.csv")))
                            (map #(str-field-to-int :id %))))
  (send student-subject concat (->> (data-table (csv/read-csv (slurp "student_subject.csv")))
                                    (map #(str-field-to-int :subject_id %))
                                    (map #(str-field-to-int :student_id %)))))

;; select-related functions...
(defn where* [data condition-func]
  (if condition-func
    (filter condition-func data)
    data))

(defn limit* [data lim]
  (if lim
    (take lim data)
    data))

(defn order-by* [data column]
  (if column
    (sort-by column data)
    data))

(defn join* [data1 column1 data2 column2]
  (for [left data1
        right data2
        :when (= (column1 left) (column2 right))]
    (merge right left)))

(defn perform-joins [data joins]
  (if (empty? joins)
    data
    (let [[col1 data2 col2] (first joins)]
      (recur (join* data col1 @(get-table data2) col2)
             (next joins)))))

(defn select [data & {:keys [where limit order-by joins]}]
  "selects data according to filters"
  (-> @data
      (perform-joins joins)
      (where* where)
      (order-by* order-by)
      (limit* limit)))
