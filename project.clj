(defproject com.dkdhub/libmisc-clj "0.3.0"
  :description "DKD/DKDHUB - the Clojure lib of miscellaneous"
  :url "http://dkdhub.com"
  :license {:name "MIT"}

  :dependencies [[cheshire "5.11.0"]
                 [commons-io "2.11.0"]
                 [clj-time "0.15.2"]
                 [com.eaio.uuid/uuid "3.2"]
                 [org.postgresql/postgresql "42.5.1"]]

  :omit-source false
  :aot [libmisc-clj.jnio-proto])