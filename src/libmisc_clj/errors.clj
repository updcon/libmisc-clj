(ns libmisc-clj.errors
  (:require [libmisc-clj.convert :as cnv])
  (:import (java.util NoSuchElementException)
           (org.postgresql.util PSQLException)))

(defmacro ^{:private true} pack-error->json
  [msg T code]
  `(cnv/json-response {:error ~msg
                       :trace (when ~T (.getLocalizedMessage ~T))}
                      ~code))

(defn wrap-exceptions
  [handler]
  (fn
    ([request]
     (try (handler request)
          (catch AssertionError ae
            (pack-error->json "Invalid parameters" ae 400))
          (catch IllegalArgumentException ia
            (pack-error->json "Illegal parameters" ia 400))
          (catch NoSuchElementException nfe
            (pack-error->json "Not found" nfe 404))
          (catch IllegalStateException ise
            (pack-error->json "Illegal state" ise 409))
          (catch PSQLException pge
            (pack-error->json "Meta DB Error" pge 400))))))