(ns libmisc-clj.errors
  (:require [libmisc-clj.convert :as cnv]
            [libmisc-clj.vars-http :as vh])
  (:import (java.util NoSuchElementException)
           (org.postgresql.util PSQLException)
           (javax.naming LimitExceededException)
           (clojure.lang ExceptionInfo)))

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

(defmacro service-not-paid! [ctx]
  `(throw (ex-info "Payment required" {:cause :not-paid :ctx ~ctx})))

(defmacro service-not-subscribed! [ctx]
  `(throw (ex-info "Not subscribed" {:cause :not-subscribed :ctx ~ctx})))

(defmacro service-limits! [type ctx]
  `(throw (ex-info "Limit reached" {:cause ~type :ctx ~ctx})))

(defn wrap-paid-access
  [handler]
  (fn
    ([request]
     (try (handler request)
          (catch ExceptionInfo ei
            (let [ctx (some-> ei ex-data :ctx)]
              (case (some-> ei ex-data :cause)
                :not-paid (if ctx (vh/http-jerror-payment-required+ ctx)
                                  vh/http-jerror-payment-required)
                :not-subscribed (if ctx (vh/http-jerror-not-subscribed+ ctx)
                                        vh/http-jerror-not-subscribed)
                :over-limit (if ctx (vh/http-jerror-limit-exceeded+ ctx)
                                    vh/http-jerror-limit-exceeded)
                :over-quota vh/http-jerror-quota-reached
                vh/http-jerror-locked)))
          (catch UnsupportedOperationException _ vh/http-jerror-quota-reached)
          (catch LimitExceededException _ vh/http-jerror-limit-exceeded)))))
