(ns libmisc-clj.hash
  (:require [clojure.string :as s])
  (:import (libmisc_clj MurMur3)))

(defn hashmap->hash64 [b]
  (let [sorted (into (sorted-map) b)
        extracted (apply str (keys sorted) (vals sorted))]
    (MurMur3/hash64 (.getBytes extracted))))

(defn- -flatten-map
  "Transform a nested map into a seq of [keyseq leaf-val] pairs"
  [m]
  (when m
    ((fn flatten-helper [keyseq m]
       (when m
         (cond
           (map? m)
           (vec (mapcat (fn [[k v]] (flatten-helper (conj keyseq k) v)) m))

           (vector? m)
           (mapv (fn [v] (flatten-helper keyseq v)) m)

           :else
           [[keyseq m]])))
     [] m)))

(defn- -normalize [v]
  (let [path (butlast v)
        val (last v)]
    (if (vector? val)
      (mapv -normalize v)
      [(->> path flatten (map name) (s/join ":") keyword) val])))

(defn flatten-map* [m]
  (flatten (map -normalize (-flatten-map m))))

(defn flatten-map [m] (apply assoc {} (flatten-map* m)))

;; Number helpers

(defn abs
  "(abs n) is the absolute value of n.
  if n is not a number - returns random value between 0 and max(int)"
  [^Number n]
  (cond
    (not (number? n)) (abs (rand-int Integer/MAX_VALUE))
    (neg? n) (- n)
    :else n))
