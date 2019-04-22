{:dev      {:global-vars         {*warn-on-reflection* true}

            :plugins             [[org.apache.maven.wagon/wagon-ssh-external "3.3.2"]
                                  [org.apache.maven.wagon/wagon-http-lightweight "3.3.2"]]

            :plugin-repositories [["private-jars" "http://local.repo:9180/repo"]]
            :deploy-repositories [["private-jars-scp" {:url              "scp://local.repo/home/clojar/data/dev_repo/"
                                                       :username         "clojar"
                                                       :private-key-file :env/clojure_ssh_key}]]

            :dependencies        [[org.clojure/clojure "1.10.0"]
                                  [org.clojure/tools.namespace "0.2.11"]]}
 :provided {
            :source-paths      #{"src-clj"}
            :resource-paths    ["resources"]

            :java-source-paths #{"src-java"}
            :javac-options     ["-source" "1.8" "-target" "1.8" "-g:none"]

            :clean-targets     ^{:protect false} [:target-path]
            }}

