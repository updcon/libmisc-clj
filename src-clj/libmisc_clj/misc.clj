(ns libmisc-clj.misc
  (:require [libmisc-clj.errors :as er]
            [clojure.string :as s]
            [clj-time.core :as t])
  (:import (java.nio.charset Charset)
           (libmisc_clj.jnio_proto IByteBuffer)
           (java.net InetAddress InetSocketAddress Socket URL)
           (org.joda.time LocalDate)
           (java.math RoundingMode)))

(defn sym->var [val]
  (-> val symbol resolve))

(def not-nil? (complement nil?))
(def nn? (complement number?))
(def nz? (complement zero?))

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

(defn int->bytes
  "Converts int value into a sequence of 4 bytes.
  The zeroes are padded to the beginning in order to make
  the BigInteger constructor working"
  [^Integer value]
  (let [pad (repeat 4 (byte 0))
        bytes (map byte (.toByteArray (BigInteger/valueOf value)))]
    (concat (drop (count bytes) pad) bytes)))

(defn long->bytes
  "Converts long value into a sequence of 8 bytes.
  The zeroes are padded to the beginning in order to make
  the BigInteger constructor working"
  [^Long value]
  (let [pad (repeat 8 (byte 0))
        bytes (map byte (.toByteArray (BigInteger/valueOf value)))]
    (concat (drop (count bytes) pad) bytes)))

(defn- in-ascii-range? [x] (<= 0 (int x) 127))

(defn- ascii? [xs]
  (every? in-ascii-range? (seq xs)))

(defn hexadecimal-string?
  "Returns truthy if `new-value` is a hexadecimal-string"
  [new-value]
  (and (string? new-value)
       (re-matches #"[0-9a-f]{64}" new-value)))

(defn url?
  "Is STRING a valid HTTP/HTTPS URL? (This only handles `localhost` and domains like `metabase.com`; URLs containing
  IP addresses will return `false`.)"
  ^Boolean [^String s]
  (boolean (when (seq s)
             (when-let [^URL url (er/ignore-exceptions (URL. s))]
               ;; these are both automatically downcased
               (let [protocol (.getProtocol url)
                     host (.getHost url)]
                 (and protocol
                      host
                      (re-matches #"^https?$" protocol)
                      (or (re-matches #"^.+\..{2,}$" host)  ; 2+ letter TLD
                          (= host "localhost"))))))))

(defn email?
  "Is STRING a valid email address?"
  ^Boolean [^String s]
  (boolean (when (string? s)
             (re-matches #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"
                         (s/lower-case s)))))

(defn base64-string?
  "Is `s` a Base-64 encoded string?"
  ^Boolean [s]
  (boolean (when (string? s)
             (re-find #"^[0-9A-Za-z/+]+=*$" s))))

(defn format-bytes
  "Nicely format `num-bytes` as kilobytes/megabytes/etc.
    (format-bytes 1024) ; -> 2.0 KB"
  [num-bytes]
  (loop [n num-bytes [suffix & more] ["B" "KB" "MB" "GB"]]
    (if (and (seq more)
             (>= n 1024))
      (recur (/ n 1024.0) more)
      (format "%.1f %s" n suffix))))

(defn index-of
  "Return index of the first element in `coll` for which `pred` reutrns true."
  [pred coll]
  (first (keep-indexed (fn [i x]
                         (when (pred x) i))
                       coll)))
(defn update-when
  "Like clojure.core/update but does not create a new key if it does not exist.
   Useful when you don't want to create cruft."
  [m k f & args]
  (if (contains? m k)
    (apply update m k f args)
    m))

(defn update-in-when
  "Like clojure.core/update-in but does not create new keys if they do not exist.
   Useful when you don't want to create cruft."
  [m k f & args]
  (if (not= ::not-found (get-in m k ::not-found))
    (apply update-in m k f args)
    m))

(def ^{:arglists '([n])} safe-inc
  "Increment N if it is non-`nil`, otherwise return `1` (e.g. as if incrementing `0`)."
  (fnil inc 0))

(defn round-to-decimals
  "Round (presumabily floating-point) NUMBER to DECIMAL-PLACE. Returns a `Double`.
     (round-to-decimals 2 35.5058998M) -> 35.51"
  ^Double [^Integer decimal-place, ^Number number]
  {:pre [(integer? decimal-place) (number? number)]}
  (double (.setScale (bigdec number) decimal-place RoundingMode/HALF_UP)))

(def ^:private ^:const host-up-timeout
  "Timeout (in ms) for checking if a host is available with `host-up?` and `host-port-up?`."
  5000)

(defn host-up?
  "Returns true if the host given by hostname is reachable, false otherwise.
   The `hostname` is always treated as name to be used to determine
   the IP address of a host. If it fails the results is also negative.
   By default, waits 5 seconds till return, this may be configured using
   an optional parameter `timeout` (time to wait in milliseconds)"
  ([^String hostname] (host-up? hostname host-up-timeout))
  ([^String hostname ^Integer timeout]
   (try
     (let [host-addr (InetAddress/getByName hostname)]
       (.isReachable host-addr timeout))
     (catch Throwable _ false))))

(defn host-port-up?
  "Returns true if the port is active on a given host, false otherwise.
   Uses `InetSocketAddress` and `Socket.connect` (i.e. reliable TCP proto) under the hood.
   The `hostname` is always treated as name to be used to determine
   the IP address of a host. If it fails the results is also negative.
   By default, waits 5 seconds till return, this may be configured using
   an optional parameter `timeout` (time to wait in milliseconds)"
  ([^String hostname ^Integer port]
   (host-port-up? hostname port host-up-timeout))
  ([^String hostname ^Integer port ^Integer timeout]
   (try
     (let [sock-addr (InetSocketAddress. hostname port)]
       (with-open [sock (Socket.)]
         (.connect sock sock-addr timeout)
         true))
     (catch Throwable _ false))))

(defn maybe?
  "Returns `true` if X is `nil`, otherwise calls (F X).
   This can be used to see something is either `nil` or statisfies a predicate function:
     (string? nil)          -> false
     (string? \"A\")        -> true
     (maybe? string? nil)   -> true
     (maybe? string? \"A\") -> true
   It can also be used to make sure a given function won't throw a `NullPointerException`:
     (s/lower-case nil)            -> NullPointerException
     (s/lower-case \"ABC\")        -> \"abc\"
     (maybe? s/lower-case nil)     -> true
     (maybe? s/lower-case \"ABC\") -> \"abc\"
   The latter use-case can be useful for things like sorting where some values in a collection
   might be `nil`:
     (sort-by (partial maybe? s/lower-case) some-collection)"
  [f x]
  (or (nil? x)
      (f x)))

(defn sequence-of-maps?
  "Is COLL a sequence of maps?"
  [coll]
  (and (sequential? coll)
       (every? map? coll)))

(defn localdate-now ^LocalDate [^String tz]
  (LocalDate/now (t/time-zone-for-id (or tz "UTC"))))

(defn literal? [^String s]
  (if (string? s)
    (-> s .trim .isEmpty not)
    false))

(defmacro when-let*
  "The extended version of when-let what allows multiple bindings"
  ([bindings & body]
   (if (seq bindings)
     `(when-let [~(first bindings) ~(second bindings)]
        (when-let* ~(drop 2 bindings) ~@body))
     `(do ~@body))))

(defmacro def-
  "Yields a private definitions as 'def the same way as defn- does"
  [name & body]
  (list* `def (with-meta name
                         (assoc (meta name) :private true))
         body))
