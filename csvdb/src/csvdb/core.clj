(ns csvdb.core
  (:gen-class)
  (:use [csvdb helpers network db]))

(defn -main [& args]
  "Initialize data and run database server on port from args or 9997"
  (let [port (if (empty? args)
               9997
               (parse-int (first args)))]
    (println "Starting the network server on port" port)
    (load-initial-data)
    (run port)
    (shutdown-agents)))
