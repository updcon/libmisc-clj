{:dev     {:global-vars         {*warn-on-reflection* true}

           :source-paths        #{"src"}
           :resource-paths      ["resources"]

           :target-path         "target/%s"
           :clean-targets       ^{:protect false} [:target-path]

           :plugins             [[lein-ancient "0.6.15"]
                                 [org.apache.maven.wagon/wagon-ssh-external "3.0.0"]
                                 [org.apache.maven.wagon/wagon-http-lightweight "3.0.0"]]

           :plugin-repositories [["private-jars" "http://10.10.3.4:9180/repo"]]
           :deploy-repositories [["private-jars-scp" {:url              "scp://10.10.3.4/home/clojar/data/dev_repo/"
                                                      :username         "clojar"
                                                      :private-key-file :env/clojure_ssh_key}]]

           :dependencies        [[org.clojure/clojure "1.9.0"]]}

 :uberjar {:aot      :all
           :jvm-opts #=(eval
                         (concat ["-Xmx1G"]
                           (let [version (System/getProperty "java.version")
                                 [major _ _] (clojure.string/split version #"\.")]
                             (if (>= (Integer. major) 9)    ;; FIXME: drop this tricky hack/hacky trick
                               ["--add-modules" "java.xml.bind"]
                               []))))}}

