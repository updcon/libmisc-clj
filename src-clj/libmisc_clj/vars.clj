(ns libmisc-clj.vars)

(defonce ^:const ^:static ttl_1m (* 1000 60))                                    ;seconds @ minute
(defonce ^:const ^:static ttl_1h (* ttl_1m 60))                                  ;mins @ hour
(defonce ^:const ^:static ttl_1d (* ttl_1h 24))                                  ;day
(defonce ^:const ^:static ttl_1w (* ttl_1d 7))                                   ;week

(def ^:const ^:static time-units-ms
  (apply hash-map
         (interleave [:millisecond :second :minute :hour :day :week]
                     (reductions * [1 1000 60 60 24 7]))))
