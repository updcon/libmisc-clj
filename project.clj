(defproject com.dkdhub/libmisc-clj "0.1.1"
  :description "DKD Core Engine"
  :url "http://dkdhub.com"
  :license {:name "Proprietary"
            :url  "https://dkdhub.com/licenses/base.html"}

  :dependencies [[cheshire "5.10.0"]
                 [commons-io "2.8.0"]
                 [clj-time "0.15.2"]
                 [com.eaio.uuid/uuid "3.2"]
                 [org.postgresql/postgresql "42.2.18"]]

  :omit-source false
  :aot [libmisc-clj.jnio-proto])

(cemerick.pomegranate.aether/register-wagon-factory!
  "scp" #(let [c (resolve 'org.apache.maven.wagon.providers.ssh.external.ScpExternalWagon)]
           (clojure.lang.Reflector/invokeConstructor c (into-array []))))
