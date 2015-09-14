(defproject clj-jpa "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [environ "1.0.0"]
                 [javax/javaee-api "7.0"]]
  :plugins [[quickie "0.4.0"]
            [lein-environ "1.0.0"]]
  :java-source-paths ["src/java"]
  :source-paths ["src/clj"]
  :javac-options ["-target" "1.7" "-source" "1.7" "-Xlint:-options"]
  :pom-plugins [[org.apache.maven.plugins/maven-compiler-plugin "3.3"
                 {:configuration ([:source "1.7"] [:target "1.7"])}]]
  :profiles {:dev {:java-source-paths ["test/java"]
                   :test-paths ["test/clj"]
                   :dependencies [[org.eclipse.persistence/org.eclipse.persistence.jpa "2.5.2"]
                                  [org.jboss.weld.se/weld-se "2.2.14.Final"]
                                  [org.slf4j/slf4j-simple "1.7.2"]
                                  [com.h2database/h2 "1.4.188"]
                                  [junit "4.12"]]}
             :weld-debug {:jvm-opts ["-Dorg.jboss.logging.provider=slf4j"
                                     "-Dorg.slf4j.simpleLogger.log.org.jboss.weld=debug"]} })
