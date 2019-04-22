(ns libmisc-clj.digest)

(def alphabet58 (vec "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"))

(def inverted-alphabet58
  (into {}
        (map #(vector %1 %2)
             alphabet58
             (iterate inc 0))))

(defn count-leading [pred s]
  (->>
    s
    (map byte)
    (take-while pred)
    count))

(defn string->bigint [base xform s]
  (reduce +
          (map #(* (xform %1) %2)
               (reverse s)
               (iterate (partial * base) 1M))))

(def divmod (juxt quot mod))

(def first-char58? (partial = (byte (first alphabet58))))

(defn emitter [base value]
  (if (pos? value)
    (let [[d m] (divmod value base)]
      (cons
        (int m)
        (lazy-seq (emitter base d))))))

(defn pipeline [from to xform map-fn drop-pred replace-ch s]
  (->>
    s
    (string->bigint from xform)
    (emitter to)
    (map map-fn)
    reverse
    (concat (repeat (count-leading drop-pred s) replace-ch))
    (apply str)))

(defn b58-encode [value]
  (pipeline 256 58 byte alphabet58 zero? (first alphabet58) value))

(defn b58-decode [value]
  (->>
    (drop-while first-char58? value)
    (pipeline 58 256 inverted-alphabet58 char first-char58? "\000")))
