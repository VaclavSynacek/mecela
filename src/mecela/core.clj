(ns mecela.core
  (:require
            [clojure.string :as str]
            [hawk.core :as hawk]
            [jansi-clj.core :as color]
            [clojure.tools.cli :as cli])
  (:gen-class))
 
 
(defn apply-rules
  [rules s]
  (remove nil? (map #(if (re-find (re-pattern (second %))  s) (first %)) rules)))

 
(defn classify-one
  [rules s]
  (let [results (apply-rules rules s)]
       (case (count results)
         0 {:result :unmatched :source s}
         1 {:result :matched :group (first results) :source s}
         {:result :overmatched :source s :error (str "Overmatched by these rules: " (str/join "-" results))})))
 

(defn classify
  [rules logs]
  (map (partial classify-one rules) logs))
 
 
(defn result-frequencies
 [results]
 (->> results
      (map :result)
      (frequencies)))
 

(defn mece
  [result-frequencies]
  (if (:overmatched result-frequencies)
      :overlapping
      (if (:unmatched result-frequencies)
          :not-exhaustive
          :mece)))


(defn pretty-format-result
  [mr]
  (case mr
    :not-exhaustive (color/white "regex rules are not exclusive, some logs are still unmatched")
    :mece (color/green "regex rules are MECE (Mutually Exclusive, Collectively Exhaustive)")
    :overlapping (color/red "regex rules are overlapping, some logs are matched by more than one regex")))
 
 
(defn unmatched-head
  [results]
  (->> results
       (filter #(= :unmatched (:result %)))
       (take 10)
       (map :source)))

 
(defn matched-frequencies
  [results]
  (->> results
       (filter #(= :matched (:result %)))
       (map :group)
       (frequencies)))
 
 
(defn get-logs
  [file]
  (->> (str/split-lines (slurp file));))
       (remove #(= % ""))))
 
 
(defn get-rules
  [file]
  (->> (str/split-lines (slurp file))
       (map #(str/split % #" " 2))))
 
 
(defn process
  [rules logs]
  (println "Running analysis now ........................")
  (let [classified (classify rules logs)
        result-frequencies (result-frequencies classified)
        matched-frequencies (matched-frequencies classified)]
       (println (color/blue "Result: " (pretty-format-result (mece result-frequencies))))
       (doall (map println result-frequencies))
       (println (color/blue "frequencies of matched ------------------------"))
       (doall (map println matched-frequencies))
       (println (color/blue "Unmatched HEAD --------------------------------"))
       (doall (map println (unmatched-head classified)))))

  
(defn start
  [log-file rules-file]
  (let [logs (get-logs log-file)
        rules (get-rules rules-file)]
    (println "Mutually Exclusive Collectively Exhaustive Log Analyser")
    (println "analyzing log " log-file " with regex definitions in " rules-file)
    (println "update the definitions and save to rerun analysis")
    (process rules logs)
    (hawk/watch!
      [{:paths [rules-file]
        :handler (fn [ctx e]
                   (time (process (get-rules rules-file) logs))
                  ctx)}])))

(def cli-options
  [["-l" "--log FILE" "Log File to analyze"]
   ["-r" "--regex FILE" "File with regex group definitions"]
   ["-h" "--help"]])

 
(defn -main [& args]
  (let [{:keys [options arguments errors summary]}
        (cli/parse-opts args cli-options)]
    (if (or (:help options) (not (:log options)) (not (:regex options)))
      (do (println "Mutually Exclusive Collectively Exhaustive Log Analyser")
          (println "Usage is: ")
          (println summary)
          (println "Example: java -jar mecela.jar -l /var/log/authlog -r sample.regex"))
      (start (:log options) (:regex options)))))
