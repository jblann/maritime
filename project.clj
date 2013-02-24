(defproject maritime "0.1"
  :description "Analytic project based on maritime shipping dataset"
  :url "http://github.com/ryankohl/maritime"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src"]
  :java-source-paths ["src/jvm"]
  :javac-options {:debug "true"}
  :resources-paths ["multilang"]
  :aot :all
  :repositories {}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [storm "0.8.2"]
                 [com.hmsonline/storm-cassandra "0.3.0"]
                 [commons-collections/commons-collections "3.2.1"]
                 [org.clojure/data.xml "0.0.7"]])
