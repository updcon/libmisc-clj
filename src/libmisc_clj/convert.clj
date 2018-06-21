(ns libmisc-clj.convert
  (:require [cheshire.core :as json]))

(defn json-response
  ([body] (json-response body 200))
  ([body code]
   {:status  code
    :headers {"Content-Type" "application/json; charset=utf-8"}
    :body    (json/generate-string body)}))

(defmacro json-error
  [msg code]
  `(json-response {:error ~msg} ~code))