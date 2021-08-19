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
