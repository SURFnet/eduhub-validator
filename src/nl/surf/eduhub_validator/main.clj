(ns nl.surf.eduhub-validator.main
  (:require [babashka.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [nl.jomco.apie.main :as apie]))

(def included-profiles
  (-> "included-profiles.txt"
      (io/resource)
      (io/reader :encoding "UTF-8")
      (line-seq)))

(def cli-options
  (mapv
   ;; insert default builtin-profile in options
   (fn [[short-opt :as option]]
     (if (= "-r" short-opt)
       ["-r" "--profile PROFILE" "Path to profile or name of builtin profile"
        :id :profile
        :default (first included-profiles)]
       option))
   apie/cli-options))

(defn app-version
  "Return version of app."
  []
  (when-let [r (io/resource "nl/surf/eduhub_validator/version.txt")]
    (string/trim (slurp r))))

(defn spec-version
  "Return version of openapi spec for built-in profile."
  [profile]
  (-> profile
      apie/file-or-resource
      apie/read-edn
      :openapi-spec
      apie/file-or-resource
      apie/read-json
      (get-in ["info" "version"])))

(defn versions
  "Return version of app and included openapi specs."
  []
  (str "eduhub-validator " (app-version) "\n"
       "included profiles:\n"
       (string/join "\n" (map (fn [p]
                                (str " - " p " " (spec-version p)))
                              included-profiles))))

(defn usage
  [summary]
  (->> ["Eduhub Validator"
        ""
        "A command-line tool to spider and validate Open Education API"
        "endpoints to ensure compatibility with services in SURFeduhub."
        ""
        "Usage: eduhub-validator [OPTIONS] [SEED...]"
        ""
        "OPTIONS:"
        summary
        ""
        "SEEDs are full URLs matching BASE-URL, or paths relative to BASE-URL."
        ""
        "If SEEDs are not provided, uses seeds in profile."
        ""
        "To validate all reachable paths from a service, use"
        "eduhub-validator --profile=rio --base-url=http://example.com"
        ""
        "To validate one specific path, use"
        "eduhub-validator --profile=rio --base-url=http://example.com -M1 '/courses/some-course-id'"]
       (string/join "\n")))

(defn -main
  [& args]
  (let [{:keys [errors summary options arguments]} (parse-opts args cli-options)]
    (when (:help options)
      (println (usage summary))
      (System/exit 0))
    (when (:print-version? options)
      (println (versions))
      (System/exit 0))
    (when (seq errors)
      (run! println errors)
      (println summary)
      (when included-profiles
        (println "\nBuiltin profiles:")
        (run! #(println " - " %) included-profiles))
      (System/exit 1))
    (when-let [invalid-seeds (seq (remove #(apie/valid-seed? (:base-url options) %) arguments))]
      (apply println "Invalid SEEDs:" invalid-seeds)
      (System/exit 1))
    (let [options  (if (seq arguments)
                     (assoc options :seeds (map #(apie/parse-seed (:base-url options) %) arguments))
                     options)]
      (apie/main options))))
