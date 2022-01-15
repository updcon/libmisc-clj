(ns libmisc-clj.digest
  (:require [clojure.string :as s])
  (:import (java.nio ByteBuffer)))

(def alphabet59 (vec "0123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"))

(defn bigint->bytes [v]
  (.toByteArray (BigInteger/valueOf v)))

(defn encode
  "Encode byte array `b` into a base59 string."
  [^bytes b]
  (if (empty? b)
    ""
    (let [s (StringBuilder.)
          zero-count (count (take-while zero? b))]
      ;; BigInteger's signum must be 1 so that b is processed unsigned
      (loop [i (BigInteger. 1 b)]
        (when-not (zero? i)
          (.append s (nth alphabet59 (mod i 59)))
          (recur (quot i 59))))
      (str (s/join (repeat zero-count "0")) (.reverse s)))))

(defn- char-index [c]
  (if-let [index (s/index-of alphabet59 c)]
    index
    (throw (ex-info (str "Character " (pr-str c) " is not part of Base59 character set.")
                    {:type ::illegal-character
                     :character c}))))
