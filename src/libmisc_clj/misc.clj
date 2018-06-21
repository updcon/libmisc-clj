(ns libmisc-clj.misc
  (:import (java.nio.charset Charset)))

(defn sym->var [sym]
  (-> sym symbol resolve))

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
  ([byte-buffer]
   (jnio->string byte-buffer (Charset/defaultCharset)))
  ([byte-buffer charset]
   (when (not-nil? byte-buffer)
     (.toString (.decode charset byte-buffer)))))

(defn string->jnio
  ([string]
   (string->jnio string (Charset/defaultCharset)))
  ([string charset]
   (.encode charset string)))