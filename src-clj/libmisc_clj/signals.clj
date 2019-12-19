(ns libmisc-clj.signals
  (:import (sun.misc Signal SignalHandler)
           (clojure.lang Keyword)))

(defn ^Signal ->signal
  "Convert a keyword to an appropriate Signal instance."
  [signal]
  (Signal. (-> signal name .toUpperCase)))

(defn ^Long signal->number
  "Find out a signal's number"
  [signal]
  (-> signal ->signal .getNumber))

(defn ^Keyword signal->kw
  "Translate a signal to a keyword"
  [^Signal s]
  (-> s .getName .toLowerCase keyword))

(defn ^SignalHandler ->handler
  "Convert class to signal handler."
  [handler]
  (proxy [SignalHandler] []
    (handle [sig] (handler (signal->kw sig)))))

(defn on-signal
  "Execute handler when signal is caught"
  [signal handler]
  (Signal/handle (->signal signal) (->handler handler)))

(defmacro with-handler
  "Install a signal handler which will execute a function
   body when a UNIX signal is caught"
  [signal & body]
  `(on-signal ~signal (fn [_#] ~@body)))
