(ns libmisc-clj.vars
  (:require [libmisc-clj.convert :as cnv]))

(defonce ^:static ttl_1m (* 1000 60))                                    ;seconds @ minute
(defonce ^:static ttl_1h (* ttl_1m 60))                                  ;mins @ hour
(defonce ^:static ttl_1d (* ttl_1h 24))                                  ;day
(defonce ^:static ttl_1w (* ttl_1d 7))                                   ;week

(defonce ^:static not-implemented (cnv/json-error "Not Implemented Yet" 501))