(ns libmisc-clj.uuid
  (:require [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.coerce :as tc])
  (:import (java.util Date UUID)
           (org.joda.time LocalDate)))

(defn get-timestamp ^long [^UUID uuid]
  (when (= 1 (.version uuid))
    (.timestamp uuid)))

(defn posix-time
  ([]
   (posix-time (System/currentTimeMillis)))
  ([^long gregorian]
   (- (quot gregorian 10000) 12219292800000)))

(defn get-instant [^UUID uuid]
  (when-let [ts (get-timestamp uuid)]
    (Date. (long (posix-time ts)))))

(defn uuid-v1->isotime [^UUID x]
  (tf/unparse (tf/formatter :date-time)
              (tc/from-date (get-instant x))))

(defn uuid-v1->isotime-tz ^LocalDate [^UUID x ^String tz]
  (-> x
      get-instant
      tc/from-date
      (t/to-time-zone (t/time-zone-for-id (or tz "UTC")))
      tc/to-local-date))

(defn uuid-v1->timestamp [^UUID x]
  (tc/to-long (long (/ (- (.timestamp x) 0x01b21dd213814000)
                       10000))))

(defn uuid-delta
  "returns diff between two uuid/v1 serails in ms"
  [^UUID u1a ^UUID u1b]
  (- (uuid-v1->timestamp u1b)
     (uuid-v1->timestamp u1a)))
