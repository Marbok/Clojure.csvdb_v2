(ns csvdb.network
  (:use [csvdb helpers query])
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.net Socket ServerSocket]))

;; variable for synchronize threads
(def ^:private should-be-finished (promise))

(defn handle-request [^Socket sock]
  (binding
    [*in* (io/reader (.getInputStream sock))
     *out* (io/writer (.getOutputStream sock))]
    (try
      (do
        (let [s (read-line)]
          (if (= (str/lower-case s) "quit")
            (deliver should-be-finished true)
            (prn (perform-query s)))))
      (catch Throwable ex
        (println "Exception: " ex))
      (finally
        (.close sock)))))


(defn- run-loop [server-sock]
  (try
    (let [^Socket sock (.accept server-sock)]
      (future (handle-request sock)))
    (catch Throwable ex
      (println "Got exception" ex)
      (deliver should-be-finished true))))


(defn run [port]
  (let [server-socket (ServerSocket. port)]
    (loop [_ (run-loop server-socket)]
      (when-not (realized? should-be-finished)
        (recur (run-loop server-socket))))
    (.close server-socket)))

;(defn run [port]
;  (let [sock-addr (InetSocketAddress. nil port)
;        server-socket (doto (ServerSocket.)
;                        (.setReuseAddress true)
;                        (.setSoTimeout 3000)
;                        (.bind sock-addr))]
;    (loop [_ (run-loop server-socket)]
;      (when-not (realized? should-be-finished)
;        (recur (run-loop server-socket))))
;    (.close server-socket)))
