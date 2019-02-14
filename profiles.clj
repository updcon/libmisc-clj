{:dev {:global-vars         {*warn-on-reflection* true}

       :source-paths        #{"src"}
       :resource-paths      ["resources"]

       :target-path         "target/%s"
       :clean-targets       ^{:protect false} [:target-path]

       :plugins             [[org.apache.maven.wagon/wagon-ssh-external "3.3.2"]
                             [org.apache.maven.wagon/wagon-http-lightweight "3.3.2"]]

       :plugin-repositories [["private-jars" "http://10.10.3.4:9180/repo"]]
       :deploy-repositories [["private-jars-scp" {:url              "scp://10.10.3.4/home/clojar/data/dev_repo/"
                                                  :username         "clojar"
                                                  :private-key-file :env/clojure_ssh_key}]]

       :dependencies        [[org.clojure/clojure "1.10.0"]
                             [org.clojure/tools.namespace "0.2.11"]]}}

