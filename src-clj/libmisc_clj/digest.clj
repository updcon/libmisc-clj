(ns libmisc-clj.digest)

(def alphabet59 (vec "0123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"))

(defn- count-leading [pred s]
  (->>
    s
    (map byte)
    (take-while pred)
    count))

(defn- string->bigint [base xform s]
  (reduce +
          (map #(* (xform %1) %2)
               (reverse s)
               (iterate (partial * base) 1M))))

(defn bigint->bytes [v]
  (.toByteArray (BigInteger/valueOf v)))

(def ^:private divmod (juxt quot mod))

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
    (concat (repeat (count-leading drop-pred s) replace-ch))
    (apply str)))

(defn b59-encode [value]
  (pipeline 256 59 byte alphabet59 zero? (first alphabet58) value))
