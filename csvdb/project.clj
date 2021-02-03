(defproject by.clojurecourse/csvdb "0.1.0"
  :description "Homework 2 for Clojure Course"
  :url "http://clojurecourse.by"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/data.csv "0.1.2"]
                 [org.clojure/core.match "1.0.0"]]
  :aot [csvdb.Database csvdb.core]
  :main csvdb.core)
