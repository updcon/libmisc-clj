(ns libmisc-clj.coercions
  (:require [libmisc-clj.misc :refer [not-nil?]])
  (:import (java.util UUID)))

(defn as-bool
  "Parse a string into a boolean, or `nil` if the string cannot be parsed."
  [^String s]
  (if
    (Boolean/parseBoolean (or s ""))
    true
    (if (= 0 (.compareToIgnoreCase s "false")) false nil)))

(defn as-double
  "Parse a string into a double, or `nil` if the string cannot be parsed."
  [^String s]
  (try
    (Double/parseDouble (or s ""))
    (catch NumberFormatException _ nil)))

(defn as-int
  "Parse a string into an integer, or `nil` if the string cannot be parsed."
  [^String s]
  (try
    (Long/parseLong (or s ""))
    (catch NumberFormatException _ nil)))

(def ^{:private true} natural-pattern
  #"^-?\d+\.?\d*([Ee]\+\d+|[Ee]-\d+|[Ee]\d+)?$")

(def ^{:private true} bigdecimal-pattern
  #"^-?\d+\.?\d*M$")

(def ^{:private true} ratio-pattern
  #"^\d+/\d+$")

(def ^{:private true} skip-numeric
  #"^0+\d+$")

(defn as-number
  "Reads a number from a string. Returns nil if not a number."
  [^String s]
  (when-let [v (.trim (or s ""))]
    (when (not (re-find skip-numeric v))
      (if (or (re-find natural-pattern v)
              (re-find bigdecimal-pattern v)
              (re-find ratio-pattern v))
        (read-string s)))))

(def ^{:private true} uuid-pattern
  #"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

(defn as-uuid
  "Parse a string into a UUID, or `nil` if the string cannot be parsed."
  [^String s]
  (when (re-matches uuid-pattern (or s ""))
    (try
      (UUID/fromString s)
      (catch IllegalArgumentException _ nil))))

(defn comp-coerce [& fns]
  "In order to get compositions like: `(comp-coerce as-uuid as-number)`"
  (fn [& args]
    (loop [ff fns]
      (when (seq ff)
        (let [res (apply (first ff) args)]
          (if (not-nil? res) res
                             (recur (rest ff))))))))

(defn uuid!
  "
  Gets string and converts it into java.util/UUID object
  If v is not string or UUID -- returns nil
  "
  [^String v]
  (condp instance? v
    String (try (UUID/fromString v)
                (catch IllegalArgumentException _ nil))
    UUID v
    nil))