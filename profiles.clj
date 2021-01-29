{:dev      {:plugins             [[org.apache.maven.wagon/wagon-ssh-external "3.4.2"]
                                  [org.apache.maven.wagon/wagon-http-lightweight "3.4.2"]]

            :plugin-repositories [["private-jars" "http://local.repo:9180/repo"]]
            :deploy-repositories [["private-jars-scp" {:url              "scp://local.repo/home/clojar/data/dev_repo/"
                                                       :username         "clojar"
                                                       :private-key-file :env/clojure_ssh_key}]]}
 :provided {:dependencies      [[org.clojure/clojure "1.10.2"]
                                [org.clojure/tools.namespace "1.1.0"]]
            :global-vars       {*warn-on-reflection* true}
            :jar-exclusions    [#"\.java"]

            :source-paths      #{"src-clj"}
            :resource-paths    ["resources"]

            :java-source-paths #{"src-java"}
            :javac-options     ["-source" "8" "-target" "1.8" "-g:none"]

            :target-path       "target"
            :clean-targets     ^{:protect false} [:target-path :compile-path]}
 :jar      {:aot :all}}

