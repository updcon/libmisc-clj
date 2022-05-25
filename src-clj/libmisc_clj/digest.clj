(ns libmisc-clj.digest
  (:require [clojure.string :as s])
  (:import (java.nio ByteBuffer)))

(def alphabetDs "0123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz") ;; i.e. b59
(def ^:private alphabetDv (vec alphabetDs))
(def basis (count alphabetDv))
(def basis-name (format "Base%s" basis))

(defn num->bytes ^bytes [^Long n]
  (drop-while
    zero?
    (-> (ByteBuffer/allocate 8) (.putLong (long n)) .array)))

(defn bytes->num [b]
  (let [buff (byte-array (map int b))]
    (biginteger buff)))

(defn- char-index [c]
  (if-let [index (s/index-of alphabetDs c)]
    index
    (throw (ex-info (str "Character " (pr-str c) " is not part of " basis-name " character set.")
                    {:type      ::illegal-character
                     :character c}))))

;; ---

(defn encode
  "Encode byte array `b` into a base59 string."
  [^Long n]
  (let [b (-> n num->bytes byte-array)
        s (StringBuilder.)
        zero-count (count (take-while zero? b))]
    ;; BigInteger's signum must be 1 so that b is processed unsigned
    (loop [i (BigInteger. 1 b)]
      (when-not (zero? i)
        (.append s (nth alphabetDv (mod i basis)))
        (recur (quot i basis))))
    (str (s/join (repeat zero-count "0")) (.reverse s))))

(defn- parse [digest]
  (let [d (s/replace digest #"^0+" "")
        dv (vec d)]
    (loop [source dv
           pairs []
           position (count dv)]
      (if (zero? position)
        pairs
        (recur (rest source) (conj pairs [position (first source)]) (dec position))))))

(defn decode [^String digest]
  (biginteger
    (reduce
      (fn [acc [position value]]
        (+ acc (* (char-index value) (Math/pow basis (dec position)))))
      0
      (parse digest))))

(dotimes)