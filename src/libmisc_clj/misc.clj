(ns libmisc-clj.misc
  (:import (java.nio.charset Charset)
           (libmisc_clj.jnio_proto IByteBuffer)))

(defn sym->var [val]
  (-> val symbol resolve))

(def not-nil? (complement nil?))

(defn in?
  "Return true if x is in coll or false otherwise"
  [x coll]
  (boolean (some #(= x %) coll)))

(defn map-values [m ks]
  (map m ks))

(defn select-values [m ks]
  ((comp vals select-keys) m ks))

(defn jnio->string
  ^String
  ([^IByteBuffer byte-buffer]
   (jnio->string byte-buffer (Charset/defaultCharset)))
  ([^IByteBuffer byte-buffer ^Charset charset]
   (when (not-nil? byte-buffer)
     (.toString (.decode charset byte-buffer)))))

(defn string->jnio
  ^IByteBuffer
  ([^String string]
   (string->jnio string (Charset/defaultCharset)))
  ([^String string ^Charset charset]
   (.encode charset string)))

(defn filter-hashmap
  "returns map filtered by a function `func`"
  [func amap]
  (into {}
        (filter (fn [[_ v]] (func v)))
        amap))

(defmacro limit-strlen
  [s max]
  `(if (string? ~s)
     (if (< ~max (count ~s)) (subs ~s 0 (- ~max 1)) ~s)
     ~s))

(defn multi-merge
  "Merges maps recursively"
  ([maps] (multi-merge {} maps))
  ([{:keys [collect?]
    :or   {collect? false}
    :as   opts} maps]
  (if (every? (some-fn map? nil?) maps)
    (apply merge-with #(multi-merge opts %&) maps)
    (if collect? (vec maps) (last maps)))))
