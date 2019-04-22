(ns libmisc-clj.execute
  "Library for shelling out from Clojure."
  (:refer-clojure :exclude [flush read-line])
  (:require [clojure.java.io :as io])
  (:import (java.util.concurrent TimeUnit TimeoutException
                                 Executors)
           (java.io StringReader StringWriter)))

;; ----- ProcSpawner section

(defn proc
  "Spin off another process. Returns the process's input stream,
  output stream, and err stream as a map of :in, :out, and :err keys
  If passed the optional :dir and/or :env keyword options, the dir
  and enviroment will be set to what you specify. If you pass
  :verbose and it is true, commands will be printed. If it is set to
  :very, environment variables passed, dir, and the command will be
  printed. If passed the :clear-env keyword option, then the process
  will not inherit its environment from its parent process."
  [& args]
  (let [[cmd args] (split-with (complement keyword?) args)
        args (apply hash-map args)
        builder (ProcessBuilder. (into-array String cmd))
        env (.environment builder)]
    (when (:clear-env args)
      (.clear env))
    (doseq [[k v] (:env args)]
      (.put env k v))
    (when-let [dir (:dir args)]
      (.directory builder (io/file dir)))
    (when (:verbose args) (apply println cmd))
    (when (= :very (:verbose args))
      (when-let [env (:env args)] (prn env))
      (when-let [dir (:dir args)] (prn dir)))
    (when (:redirect-err args)
      (.redirectErrorStream builder true))
    (let [process (.start builder)]
      {:out     (.getInputStream process)
       :in      (.getOutputStream process)
       :err     (.getErrorStream process)
       :process process})))

(defn destroy
  "Destroy a process."
  [process]
  (.destroy (:process process)))

;; .waitFor returns the exit code. This makes this function useful for
;; both getting an exit code and stopping the thread until a process
;; terminates.
(defn wait-for
  "Waits for the process to terminate (blocking the thread) and returns
   the exit code. If timeout is passed, it is assumed to be milliseconds
   to wait for the process to exit. If it does not exit in time, it is
   killed (with or without fire)."
  ([process] (.waitFor (:process process)))
  ([process timeout]
   (try
     (.get (future (.waitFor (:process process))) timeout TimeUnit/MILLISECONDS)
     (catch Exception e
       (if (or (instance? TimeoutException e)
               (instance? TimeoutException (.getCause e)))
         (do (destroy process)
             :timeout)
         (throw e))))))

(defn flush-stdin
  "Flush the output stream of a process."
  [process]
  (.flush (:in process)))

(defn close-stdin
  "Close the process's output stream (sending EOF)."
  [proc]
  (-> proc :in .close))

(defn proc->>
  "Stream :out or :err from a process to an ouput stream.
  Options passed are fed to clojure.java.io/copy. They are :encoding to
  set the encoding and :buffer-size to set the size of the buffer.
  :encoding defaults to UTF-8 and :buffer-size to 1024."
  [process from to & args]
  (apply io/copy (process from) to args))

(defn ->>proc
  "Feed to a process's input stream with optional. Options passed are
  fed to clojure.java.io/copy. They are :encoding to set the encoding
  and :buffer-size to set the size of the buffer. :encoding defaults to
  UTF-8 and :buffer-size to 1024. If :flush is specified and is false,
  the process will be flushed after writing."
  [process from & {flush? :flush :or {flush? true} :as all}]
  (apply io/copy from (:in process) all)
  (when flush? (flush-stdin process)))

(defn proc->>str
  "Streams the output of the process to a string and returns it."
  [process ch & args]
  (with-open [writer (StringWriter.)]
    (apply proc->> process ch writer args)
    (str writer)))

;; The writer that Clojure wraps System/out in for *out* seems to buffer
;; things instead of writing them immediately. This wont work if you
;; really want to stream stuff, so we'll just skip it and throw our data
;; directly at System/out.
(defn proc->>stdout
  "Streams the output of the process to System/out"
  [process ch & args]
  (apply proc->> process ch (System/out) args))

(defn string->>proc
  "Feed the process some data from a string."
  [process s & args]
  (apply ->>proc process (StringReader. s) args))

(defn line<<-proc
  "Read a line from a process' :out or :err."
  [process ch]
  (binding [*in* (io/reader (ch process))]
    (clojure.core/read-line)))

;; ----- ThreadPool section

(defprotocol pExecutor
  (exec
    [p r]
    [p r callback id]
    "execute Runnable at thread asynchronously")
  (self [p] "return the pool object")
  (stop [p] "shutdown the pool"))

(defn arena
  []
  (let [executor-svc (Executors/newCachedThreadPool
                       (Executors/defaultThreadFactory))]
    (reify
      pExecutor
      (exec [_ r]
        (.execute executor-svc ^Runnable r))
      (exec [_ r callback opts]
        (.execute
          executor-svc
          ^Runnable
          (fn [] (let [data (try (r)
                                 (catch Exception e {:error (.getMessage e)}))]
                   (callback data opts))))
        opts)
      (self [_] executor-svc)
      (stop [_] (.shutdown executor-svc)))))