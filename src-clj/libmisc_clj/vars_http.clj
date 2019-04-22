(ns libmisc-clj.vars-http
  (:require [libmisc-clj.convert :as cnv]))

(defmacro json-error+ctx
  [msg code ctx]
  `(cnv/json-response {:error ~msg :context ~ctx} ~code))

(def http-jerror-payment-required (cnv/json-error "Payment required" 402))
(def http-jerror-wrong-key (cnv/json-error "Wrong key" 403))
(def http-jerror-permission-denied (cnv/json-error "Permission denied" 403))
(def http-jerror-path-not-found (cnv/json-error "Path not found" 404))
(def http-jerror-unable-create (cnv/json-error "Unable create" 409))
(def http-jerror-keyword-gone (cnv/json-error "Keywords not found" 410))
(def http-jerror-token-gone (cnv/json-error "Unable to connect resource specified" 410))
(def http-jerror-limit-exceeded (cnv/json-error "Limit exceeded" 416))
(def http-jerror-not-updated (cnv/json-error "Not updated" 421))
(def http-jerror-locked (cnv/json-error "Resource Locked" 423))
(def http-jerror-not-subscribed (cnv/json-error "Not subscribed" 424))
(def http-jerror-quota-reached (cnv/json-error "Quota reached" 428))
(def http-jerror-not-implemented (cnv/json-error "Not implemented" 501))

;; Conditional codes to cover paid content needs

(defmacro http-jerror-payment-required+
  [ctx]
  `(json-error+ctx "Payment required" 402 ~ctx))

(defmacro http-jerror-limit-exceeded+
  [ctx]
  `(json-error+ctx "Limit exceeded" 416 ~ctx))

(defmacro http-jerror-not-subscribed+
  [ctx]
  `(json-error+ctx "Not subscribed" 424 ~ctx))

;; Specific case to report that request was blocked
;; by Risk Control Engine.
;; May (but have not to!) provide additional information
;; about context. This's a final state that can't be forced
;; without additional activity

(defmacro http-error-blocked-by-rce+
  [ctx]
  `(json-error+ctx "Request blocked by system" 510 ~ctx))
