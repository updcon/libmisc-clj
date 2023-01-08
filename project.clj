(defproject com.dkdhub/libmisc-clj "1.0.2"
  :description "DKD/DKDHUB - the Clojure lib of miscellaneous"
  :url "https://github.com/updcon/libmisc-clj"
  :license {:name "MIT"}

  :dependencies [[metosin/jsonista "0.3.7"]
                 [commons-io "2.11.0"]
                 [clj-time "0.15.2"]
                 [com.eaio.uuid/uuid "3.2"]
                 [org.postgresql/postgresql "42.5.1"]]

  :omit-source false
  :aot [libmisc-clj.jnio-proto])
