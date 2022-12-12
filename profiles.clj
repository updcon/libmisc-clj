{:dev      {}
 
 :provided {:dependencies      [[org.clojure/clojure "1.11.1"]
                                [org.clojure/tools.namespace "1.3.0"]]
            :global-vars       {*warn-on-reflection* true}
            :jar-exclusions    [#"\.java"]

            :source-paths      #{"src-clj"}
            :resource-paths    ["resources"]

            :java-source-paths #{"src-java"}

            :target-path       "target"
            :clean-targets     ^{:protect false} [:target-path :compile-path]}

 :j8       {:javac-options ["-source" "8" "-target" "8" "-g:none"]}

 :j11      {:javac-options ["-source" "11" "-target" "11" "-g:none"]}

 :jar      {:aot :all}}

