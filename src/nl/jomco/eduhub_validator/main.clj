(ns nl.jomco.eduhub-validator.main
  (:require [clojure.data.json :as data.json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [nl.jomco.eduhub-validator.report :as report]
            [nl.jomco.eduhub-validator.spider :as spider]))

(defn parse-header
  [s]
  (let [[k v] (string/split s #": +" 2)]
    (when-not v
      (throw (ex-info (str "Can't parse header '" s "'") {})))
    [(keyword (string/lower-case k)) v]))

(defn add-header
  [headers [k v]]
  (update-in headers [k] spider/merge-header-values v))


(def cli-options
  [["-s" "--spec OPENAPI-PATH" "Path to OpenAPI JSON specification for validation."
    :missing "OPENAPI-PATH is missing"]
   ["-r" "--rules RULES-PATH" "Path to rules for spidering. Required when spidering."]
   ["-u" "--base-url URL" "Base URL of service to validate."
    :missing "BASE-URL is missing"]
   ["-o" "--observations OBSERVATIONS-PATH" "Path to read/write spidering observations."
    :default "observations.edn"]
   ["-p" "--report REPORT-PATH" "Path to write report."
    :default "report.html"]
   ["-S" "--no-spider" "Disable spidering (re-use observations from OBSERVATIONS-PATH)."
    :id :spider?
    :default true
    :parse-fn not]
   ["-P" "--no-report" "Disable report generation (spidering will write observations)."
    :id :report?
    :default true
    :parse-fn not]
   ["-h" "--add-header 'HEADER: VALUE'" "Add header to request. Can be used multiple times."
    :default {}
    :id :headers
    :multi true
    :parse-fn parse-header
    :update-fn add-header]
   ["-b" "--bearer-token TOKEN"
    "Add bearer token to request."
    :default nil]
   ["-a" "--basic-auth 'USER:PASS'" "Send basic authentication header."
    :default nil
    :parse-fn (fn [s]
                (let [[user pass] (string/split s #": *")]
                  (when-not pass
                    (throw (ex-info "Can't parse basic-auth" {:s s})))
                  {:user user
                   :pass pass}))]])

(defn read-edn
  [f]
  (with-open [in (java.io.PushbackReader. (io/reader f :encoding "UTF-8"))]
    (edn/read in)))

(defn read-json
  [f]
  (data.json/read-json (io/reader f :encoding "UTF-8") false))

(defn -main
  [& args]
  (let [{:keys [errors summary]
         {:keys [spider? report?
                 spec report
                 observations rules
                 base-url] :as options} :options} (parse-opts args cli-options)]
    (when (seq errors)
      (run! println errors)
      (println summary)
      (System/exit 1))
    (let [spec-data (read-json spec)
          rules-data (read-edn rules)]
      (when spider?
        (println "Spidering" base-url)
        (with-open [w (io/writer observations :encoding "UTF-8")]
          (.write w "[")
          (run! #(do (println (:uri (:request %)))
                     (pprint/pprint % w)) (spider/spider-and-validate spec-data rules-data options))
          (.write w "]")))
      (when report?
        (println "Writing report to" report)
        (binding [*out* (io/writer report :encoding "UTF-8")]
          (println
           ;; str needed to coerce hiccup "rawstring"
           (str (report/report spec-data (read-edn observations)))))))))
