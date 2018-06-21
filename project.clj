(defproject com.dkdhub/libmisc-clj "0.0.1"
  :description "DKD Core Engine"
  :url "http://dkdhub.com"
  :license {:name "Proprietary"
            :url  "https://dkdhub.com/license.html"}

  :dependencies [[cheshire "5.8.0"]
                 [org.postgresql/postgresql "42.2.2"]]

  :omit-source true)

(cemerick.pomegranate.aether/register-wagon-factory!
  "scp" #(let [c (resolve 'org.apache.maven.wagon.providers.ssh.external.ScpExternalWagon)]
           (clojure.lang.Reflector/invokeConstructor c (into-array []))))