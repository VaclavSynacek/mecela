(ns mecela.core
  (:require
            [clojure.string :as str]
            [hawk.core :as hawk]
            [jansi-clj.core :as color]
            [iota :as io]
            [clojure.core.reducers :as r])
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
         {:result :overmatched :source s :error (str "ERROR: " (str/join "-" results))})))
 

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


(defn colorful-mece-result
  [mr]
  (case mr
    :not-exhaustive (color/white :not-exhaustive)
    :mece (color/green :mece)
    :overlapping (color/red :overlapping)))
 
 
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
       (println (color/blue "Mece Result: " (colorful-mece-result (mece result-frequencies))))
       (doall (map println result-frequencies))
       (println (color/blue "frequencies of matched ------------------------"))
       (doall (map println matched-frequencies))
       (println (color/blue "Unmatched HEAD --------------------------------"))
       (doall (map println (unmatched-head classified)))))
 
 
(defn -main []
  (let [
        log-file "/var/log/authlog"
        logs (get-logs log-file)
        rules-file "sample.regex"
        rules (get-rules rules-file)]
    (println "Mutually Exclusive Commonly Exhaustive Log Analyser")
    (println "analyzing log " log-file " with regex definitions in " rules-file)
    (println "update the definitions and save to rerun analysis")
    (process rules logs)
    (hawk/watch!
      [{:paths [rules-file]
        :handler (fn [ctx e]
                   (time (process (get-rules rules-file) logs))
                ctx)}])))
