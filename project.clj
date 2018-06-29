(defproject com.dkdhub/libmisc-clj "0.0.4"
  :description "DKD Core Engine"
  :url "http://dkdhub.com"
  :license {:name "Proprietary"
            :url  "https://dkdhub.com/license.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [cheshire "5.8.0"]
                 [commons-io "2.6"]
                 [org.postgresql/postgresql "42.2.2"]]

  :omit-source false)

(cemerick.pomegranate.aether/register-wagon-factory!
  "scp" #(let [c (resolve 'org.apache.maven.wagon.providers.ssh.external.ScpExternalWagon)]
           (clojure.lang.Reflector/invokeConstructor c (into-array []))))