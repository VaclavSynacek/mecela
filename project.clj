(defproject mecela "0.1"
  :description "Mutually Exlusive Collectively Exhaustive Log Analyzer"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [hawk "0.2.11"]
                 [jansi-clj "0.1.1"]
                 [org.clojure/tools.cli "0.4.1"]]
  :aot [mecela.core]
  :main mecela.core)
 
;  :jvm-opts ["-Xms1200m"])
