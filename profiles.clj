{:dev      {:plugins             [[org.apache.maven.wagon/wagon-ssh-external "3.3.4"]
                                  [org.apache.maven.wagon/wagon-http-lightweight "3.3.4"]]

            :plugin-repositories [["private-jars" "http://local.repo:9180/repo"]]
            :deploy-repositories [["private-jars-scp" {:url              "scp://local.repo/home/clojar/data/dev_repo/"
                                                       :username         "clojar"
                                                       :private-key-file :env/clojure_ssh_key}]]

            :dependencies        [[org.clojure/clojure "1.10.1"]
                                  [org.clojure/tools.namespace "0.3.1"]]}
 :provided {:global-vars       {*warn-on-reflection* true}
            :jar-exclusions    [#"\.java"]

            :source-paths      #{"src-clj"}
            :resource-paths    ["resources"]

            :java-source-paths #{"src-java"}
            :javac-options     ["-source" "8" "-target" "1.8" "-g:none"]

            :target-path       "target"
            :clean-targets     ^{:protect false} [:target-path :compile-path]}}

