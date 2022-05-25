(ns libmisc-clj.digest
  (:import (clojure.lang LazySeq)))

(def alphabetDs "0123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz") ;; i.e. b59
(def ^:private alphabetDv (vec alphabetDs))
(def basis (count alphabetDv))
(def basis-name (format "Base%s" basis))
(def ^:private ascii 256)

(def ^:private alphabetInv
  (into {}
        (map #(vector %1 %2)
             alphabetDv
             (iterate inc 0))))

(def ^:private first-char? (partial = (byte (first alphabetDv))))

(def ^:private divmod (juxt quot mod))

(defn- string->bigint [base xform s]
  (reduce +
          (map #(* (xform %1) %2)
               (reverse s)
               (iterate (partial * base) 1M))))

(defn- count-leading [pred s]
  (->> s (map byte) (take-while pred) count))

(defn- emitter [base value]
  (if (pos? value)
    (let [[d m] (divmod value base)]
      (cons
        (int m)
        (lazy-seq (emitter base d))))))

(defn- pipeline [from to xform map-fn drop-pred replace-ch s]
  (->>
    s
    (string->bigint from xform)
    (emitter to)
    (map map-fn)
    reverse
    (concat (repeat (count-leading drop-pred s) replace-ch))))

(defn encode ^String [^String value]
  (apply str (pipeline ascii basis byte alphabetDv zero? (first alphabetDv) value)))

(defn decode ^LazySeq [^String value]
  (->>
    (drop-while first-char? value)
    (pipeline basis ascii alphabetInv char first-char? "\000")))

(defn num->bytes [n] (.toByteArray (biginteger n)))
