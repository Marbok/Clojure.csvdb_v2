(ns csvdb.Database
  (:use [csvdb db query])
  ;; Generate java class "csvdb.Database" with 2 static method
  ;;  - void InitDatabase() - initialize data
  ;;  - String Select(String query) - execute select query and return result as EDN
  (:gen-class
    :main false
    :methods
    [^:static [Select [String] String]
     ^:static [InitDatabase [] void]]))

(defn -InitDatabase []
  (load-initial-data))

(defn -Select [query]
  (pr-str (perform-query query)))