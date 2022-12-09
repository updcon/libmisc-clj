(ns libmisc-clj.errors
  (:require [libmisc-clj.convert :as cnv]
            [libmisc-clj.vars-http :as vh])
  (:import (java.util NoSuchElementException)
           (javax.xml.crypto NoSuchMechanismException)
           (org.postgresql.util PSQLException)
           (javax.naming LimitExceededException)
           (clojure.lang ExceptionInfo)
           (java.util.concurrent TimeoutException Future)))

(defmacro ^{:private true} pack-error->json
  [msg T code]
  `(cnv/json-response {:error ~msg
                       :trace (when ~T (.getLocalizedMessage ~T))}
                      ~code))

(defn illegal-argument! [fmt & args]
  (let [^String msg (apply format (str fmt) args)]
    (throw (IllegalArgumentException. msg))))

(defn illegal-state! [fmt & args]
  (let [^String msg (apply format (str fmt) args)]
    (throw (IllegalStateException. msg))))

(defn access-denied! [fmt & args]
  (let [^String msg (apply format (str fmt) args)]
    (throw (IllegalMonitorStateException. msg))))

(defn not-authorized! [fmt id & args]
  (let [^String msg (apply format (str fmt) args)]
    (throw (NoSuchMechanismException. msg))))

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
          (catch IllegalMonitorStateException ims
            (pack-error->json "Forbidden" ims 403))
          (catch NoSuchMechanismException nsme
            (pack-error->json "Not Authorized" nsme 401))
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

(defmacro ignore-exceptions
  "Simple macro which wraps the given expression in a try/catch block and ignores the exception if caught."
  [& body]
  `(try ~@body (catch Throwable ~'_)))

(defn do-with-auto-retries*
  "Execute F, a function that takes no arguments, and return the results.
   If F fails with an exception, retry F up to NUM-RETRIES times until it succeeds."
  [num-retries f]
  (if (<= num-retries 0)
    (f)
    (try (f)
         (catch Throwable e
           (do-with-auto-retries* (dec num-retries) f)))))

(defmacro auto-retry
  "Execute BODY and return the results.
   If BODY fails with an exception, retry execution up to NUM-RETRIES times until it succeeds."
  [num-retries & body]
  `(do-with-auto-retries* ~num-retries
                          (fn [] ~@body)))

(defn deref-with-timeout
  "Call `deref` on a something derefable (e.g. a future or promise), and throw an exception if it takes more than
  `timeout-ms`. If `ref` is a future it will attempt to cancel it as well."
  [reff timeout-ms]
  (let [result (deref reff timeout-ms ::timeout)]
    (when (= result ::timeout)
      (when (instance? Future reff)
        (future-cancel reff))
      (throw (TimeoutException. (format "Timed out after %d milliseconds." timeout-ms))))
    result))

(defmacro with-timeout
  "Run BODY in a `future` and throw an exception if it fails to complete after TIMEOUT-MS."
  [timeout-ms & body]
  `(deref-with-timeout (future ~@body) ~timeout-ms))
