(ns csvdb.query
  (:use [csvdb helpers db]
        [clojure.core.match :only (match)]))


;; Function for parsing input query
;;
;; Query's syntax:
;; SELECT table_name [WHERE column comp-op value] [ORDER BY column] [LIMIT N] [JOIN other_table ON left_column = right_column]
;;
;; - Columns names are string, not keywords
;; - WHERE operators: =, !=, <, >, <=, >=
;; - Table's name in JOIN is string
;; - value is number or string in single quotes
;; - keywords -> case-insensitive
;;
;; Function return list with next structure:
;; - table name as string
;; - rest arguments, which to be passed to select
;;
;; Not valid query return "nil"

;; Examples:
;; > (parse-select "select student")
;; ("student")
;; > (parse-select "select student where id = 10")
;; ("student" :where #(= (:id %) 10))
;; > (parse-select "select student where id = 10 limit 2")
;; ("student" :where #(= (:id %) 10) :limit 2)
;; > (parse-select "select student where id = 10 order by id limit 2")
;; ("student" :where #(= (:id %) 10) :order-by :id :limit 2)
;; > (parse-select "select student where id = 10 order by id limit 2 join subject on id = sid")
;; ("student" :where #(= (:id %) 10) :order-by :id :limit 2 :joins [[:id "subject" :sid]])
;; > (parse-select "werfwefw")
;; nil

(defn make-where-function [& args]
  (let [[columnStr op value] args
        column (keyword columnStr)
        parse-value (fn [value]
                      (try
                        (parse-int value)
                        (catch NumberFormatException _
                          (.replace value "'" ""))))
        result-fun (fn [op]
                     #(op ((keyword column) %) (parse-value value)))]
    (case op
      "=" (result-fun =)
      "!=" (result-fun not=)
      "<" (result-fun <)
      "<=" (result-fun <=)
      ">" (result-fun >)
      ">=" (result-fun >=))))

(defn- match-select [req res]
  (let [drop-req (fn [n] (vec (drop n req)))]
    (match req
           ["select" tbl & _] (match-select (drop-req 2) [tbl])
           ["where" column comp-op value & _] (match-select (drop-req 4) (conj res :where (make-where-function column comp-op value)))
           ["order" "by" column & _] (match-select (drop-req 3) (conj res :order-by (keyword column)))
           ["limit" n & _] (match-select (drop-req 2) (conj res :limit (parse-int n)))
           ["join" tbl "on" left "=" right] (conj res :joins [[(keyword left) tbl (keyword right)]])
           :else res)))

(defn parse-select [^String sel-string]
  (let [res (match-select (vec (.split (.toLowerCase sel-string) " ")) nil)]
    (if (nil? res)
      res
      (apply list res))))


;; Consume query and execute it. Throw exception, if can't parse query

;; Example:
;; > (perform-query "select student")
;; ({:id 1, :year 1998, :surname "Ivanov"} {:id 2, :year 1997, :surname "Petrov"} {:id 3, :year 1996, :surname "Sidorov"})
;; > (perform-query "select student order by year")
;; ({:id 3, :year 1996, :surname "Sidorov"} {:id 2, :year 1997, :surname "Petrov"} {:id 1, :year 1998, :surname "Ivanov"})
;; > (perform-query "select student where id > 1")
;; ({:id 2, :year 1997, :surname "Petrov"} {:id 3, :year 1996, :surname "Sidorov"})
;; > (perform-query "not valid")
;; exception...
(defn perform-query [^String sel-string]
  (if-let [query (parse-select sel-string)]
    (apply select (get-table (first query)) (rest query))
    (throw (IllegalArgumentException. (str "Can't parse query: " sel-string)))))
