(ns libmisc-clj.func)

(defmacro ?>>
  "Conditional double-arrow operation (->> nums (?>> inc-all? (map inc)))"
  [do-it? & args]
  `(if ~do-it?
     (->> ~(last args) ~@(butlast args))
     ~(last args)))

(defmacro ?>
  "Conditional single-arrow operation (-> m (?> add-kv? (assoc :k :v)))"
  [arg do-it? & rest]
  `(if ~do-it?
     (-> ~arg ~@rest)
     ~arg))

(defmacro fn->
  "Equivalent to `(fn [x] (-> x ~@body))"
  [& body]
  `(fn [x#] (-> x# ~@body)))

(defmacro fn->>
  "Equivalent to `(fn [x] (->> x ~@body))"
  [& body]
  `(fn [x#] (->> x# ~@body)))

(defmacro as->>
  "Like as->, but can be used in double arrow."
  [name & forms-and-expr]
  `(as-> ~(last forms-and-expr) ~name ~@(butlast forms-and-expr)))

(defn sum
  "Return sum of (f x) for each x in xs"
  ([f xs] (reduce + (map f xs)))
  ([xs] (reduce + xs)))

(defn invert-map [m to]
  "Turn `{k v}` into `{v k}`. Duplications will be vectorized.
   Second parameter `to` is to get empty collection instance for vectoring"
  (persistent!
    (reduce (fn [m [k v]]
              (assoc! m v (conj (get m v to) k)))
            (transient {}) m)))

(defn update-vals [m f & args]
  "Applies function `f` to every value with optional `args`.
   Returns new map built in constant time."
  (persistent!
    (reduce (fn [r [k v]]
              (assoc! r k (apply f v args)))
            (transient {}) m)))

(defn deep-merge-with
  "Recursively merges maps. Applies function f when sees duplicated keys"
  [f & maps]
  (letfn [(m [& xs]
            (if (some #(and (map? %) (not (record? %))) xs)
              (apply merge-with m xs)
              (apply f xs)))]
    (reduce m maps)))
