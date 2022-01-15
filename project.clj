(defproject com.dkdhub/libmisc-clj "0.2.4"
  :description "DKD Core Engine lib of miscellaneous"
  :url "http://dkdhub.com"
  :license {:name "Proprietary"
            :url  "https://dkdhub.com/licenses/base.html"}

  :dependencies [[cheshire "5.10.1"]
                 [commons-io "2.11.0"]
                 [clj-time "0.15.2"]
                 [com.eaio.uuid/uuid "3.2"]
                 [org.postgresql/postgresql "42.2.23"]]

  :omit-source false
  :aot [libmisc-clj.jnio-proto])

(cemerick.pomegranate.aether/register-wagon-factory!
  "scp" #(let [c (resolve 'org.apache.maven.wagon.providers.ssh.external.ScpExternalWagon)]
           (clojure.lang.Reflector/invokeConstructor c (into-array []))))
