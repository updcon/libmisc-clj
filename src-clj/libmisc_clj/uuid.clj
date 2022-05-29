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

(defn uuid-v1->timestamp ^long [^UUID x]
  (some-> x get-timestamp posix-time long))

(defn get-instant [^UUID uuid]
  (some-> uuid uuid-v1->timestamp Date.))

(defn- unformat*
  ([td fmt & [local]]
  (when td
     ((if local tf/unparse-local tf/unparse)
      (tf/formatter fmt)
      (if local td (tc/from-date td))))))

(defn uuid-v1->isotime [^UUID x]
  (unformat* (get-instant x) :date-time))

(defn uuid-v1->isodate [^UUID x]
  (unformat* (get-instant x) :date))

(defn- uuid-v1->iso-tz* [^UUID x ^String tz]
  (some-> x
          get-instant
          tc/from-date
          (t/to-time-zone (t/time-zone-for-id (or tz "UTC")))
          tc/to-local-date-time))

(defn uuid-v1->isotime-tz ^LocalDate [^UUID x ^String tz]
  (some-> x
          (uuid-v1->iso-tz* tz)
          (unformat* :date-time true)))

(defn uuid-v1->isodate-tz ^LocalDate [^UUID x ^String tz]
  (some-> x
          (uuid-v1->iso-tz* tz)
          (unformat* :date true)))

(defn uuid-delta
  "returns diff between two uuid/v1 serails in ms"
  [^UUID u1a ^UUID u1b]
  (- (uuid-v1->timestamp u1b)
     (uuid-v1->timestamp u1a)))
