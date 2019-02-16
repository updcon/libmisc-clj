(defproject com.dkdhub/libmisc-clj "0.0.7"
  :description "DKD Core Engine"
  :url "http://dkdhub.com"
  :license {:name "Proprietary"
            :url  "https://dkdhub.com/licenses/base.html"}

  :dependencies [[cheshire "5.8.1"]
                 [commons-io "2.6"]
                 [org.postgresql/postgresql "42.2.5"]]

  :omit-source false
  :aot [libmisc-clj.jnio-proto])

(cemerick.pomegranate.aether/register-wagon-factory!
  "scp" #(let [c (resolve 'org.apache.maven.wagon.providers.ssh.external.ScpExternalWagon)]
           (clojure.lang.Reflector/invokeConstructor c (into-array []))))
