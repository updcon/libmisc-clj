(ns libmisc-clj.web
  (:require [libmisc-clj.misc :as hc]
            [libmisc-clj.coercions :refer :all])
  (:import (org.apache.commons.io IOUtils)
           (java.io InputStream)
           (java.net URLEncoder)))

(defn- reconstruct-single [v]
  (cond
    (string? v)
    (let [res ((comp-coerce as-bool as-number as-uuid) v)]
      (if (hc/not-nil? res) res v))

    :else v))

(defn reconstruct-vals [target]
  (cond
    (map? target)
    (into {}
          (for [[k v] target]
            [k (reconstruct-vals v)]))

    (vector? target)
    (mapv reconstruct-vals target)

    :else
    (reconstruct-single target)))

(defn- value-params-request
  "Converts string values in :params map to native types."
  [request]
  (update request :params reconstruct-vals))

(defn wrap-params-values
  "Middleware that converts the any string values in the :params map
   to their native representation.
  Only vals that can be turned into valid native format are converted.
  Other values stay unaffected."
  [handler]
  (fn
    ([request]
     (handler (value-params-request request)))
    ([request respond raise]
     (handler (value-params-request request) respond raise))))

(defn- keyword-syntax? [s]
  (re-matches #"[\p{L}*+!_?-][\p{L}0-9*+!_?-]*" s))

(defn keyify-params [target]
  (cond
    (map? target)
    (into {}
          (for [[k v] target]
            [(if (and (string? k) (keyword-syntax? k))
               (keyword k)
               k)
             (keyify-params v)]))
    (vector? target)
    (vec (map keyify-params target))
    :else
    target))

(def ->native (comp keyify-params reconstruct-vals))

(defn paginated-list [alist]
  {:total (-> alist first :total (or 0)) :items alist})

(defn downloaded-file [name body]
  (let [name (URLEncoder/encode (str name) "UTF-8")
        file (str "attachment; filename=" name)
        content "application/octet-stream"]
    {:status  200
     :headers {"Content-Type"        content
               "Content-Disposition" file}
     :body    body}))

(defn byte-array-store
  "Returns a function that stores multipart file parameters as an array of
  bytes. The multipart parameters will be stored as maps with the following
  keys:
  :filename     - the name of the uploaded file
  :content-type - the content type of the uploaded file
  :bytes        - an array of bytes containing the uploaded content"
  []
  (fn [item]
    (-> (select-keys item [:filename :content-type])
        (assoc :bytes (IOUtils/toByteArray ^InputStream (:stream item))))))

(defn empty-store [item]
  (let [{filename     :filename
         content-type :content-type
         stream       :stream
         :as          params} item]
    item))
