(ns libmisc-clj.digest
  (:require [clojure.string :as s])
  (:import (java.nio ByteBuffer)
           (java.util UUID)))

(def alphabetDs "0123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz") ;; i.e. b59 & '= as a signum for negatives
(def ^:private alphabetDv (vec alphabetDs))
(def basis (count alphabetDv))
(def basis-name (format "Base%s" basis))

(defn- num->signed-bytes [num, size]
  (let [byte-array (-> (str num) (BigInteger.) .toByteArray)]
    (let [pad-byte (if (neg? num) (byte -1) (byte 0))]
      (apply vector (concat
                      (repeat (- size (count byte-array)) pad-byte)
                      (take-last (min size (count byte-array)) byte-array))))))

(defn num->unsigned-bytes [num, size]
  (let [byte-array (-> (str num) (BigInteger.) .toByteArray)]
    (apply vector (concat
                    (repeat (- size (count byte-array)) (byte 0))
                    (take-last (min size (count byte-array)) byte-array)))))

(defn signed-bytes->num [& bytes]
  (BigInteger. (byte-array (flatten bytes))))

(defn unsigned-bytes->num [& bytes]
  (BigInteger. (byte-array (cons (byte 0) (flatten bytes)))))

(defn num->bytes ^bytes [^Long n]
  (drop-while zero? (num->signed-bytes n 8)))

(defn bytes->num [b]
  (signed-bytes->num b))

(defn- char-index [c]
  (if-let [index (s/index-of alphabetDs c)]
    index
    (throw (ex-info (str "Character " (pr-str c) " is not part of " basis-name " character set.")
                    {:type      ::illegal-character
                     :character c}))))

(def ^:private divmod (juxt quot mod))
;; ---

(defn encode
  "Encode byte array `b` into a base59 string."
  ^String [^Long n]
  (let [[negative? value] (if (neg? n) [true (- n)] [false n])
        b (-> value num->bytes byte-array)
        s (StringBuilder.)
        zero-count (count (take-while zero? b))]
    ;; BigInteger's signum must be 1 so that b is processed unsigned
    (loop [i (BigInteger. 1 b)]
      (when-not (zero? i)
        (.append s (nth alphabetDv (mod i basis)))
        (recur (quot i basis))))
    (str (when negative? "=") (s/join (repeat zero-count "0")) (.reverse s))))

(defn- parse [digest]
  (let [d (s/replace digest #"^0+" "")
        dv (vec d)]
    (loop [source dv
           pairs []
           position (count dv)]
      (if (zero? position)
        pairs
        (recur (rest source) (conj pairs [position (first source)]) (dec position))))))

(defn decode ^Long [^String digest]
  (let [negative? (s/starts-with? digest "=")
        body (if negative? (s/replace-first digest "=" "") digest)
        value (biginteger
                (reduce
                  (fn [acc [position value]]
                    (+ acc (* (char-index value) (biginteger (.pow (biginteger basis) (dec position))))))
                  (biginteger 0)                            ;; start value to be the same with resulting
                  (parse body)))]
    (if negative? (- value) value)))

(defn encode-uuid ^String [^UUID id]
  (prn (.getMostSignificantBits id) "-" (.getLeastSignificantBits id))
  (if (uuid? id)
    (str (encode (.getMostSignificantBits id))
         "-"
         (encode (.getLeastSignificantBits id)))
    (throw (ex-info (str "ID " id " is not of type UUID")
                    {:type ::illegal-input-type
                     :id   id}))))

(defn decode-uuid ^UUID [^String id]
  (if (re-matches #"=?\w+-=?\w+" id)
    (let [[msp lsp] (s/split id #"-")]
      (UUID. (decode msp) (decode lsp)))
    (throw (ex-info (str "ID " id " is not of type UUID")
                    {:type ::illegal-input-type
                     :id   id}))))
