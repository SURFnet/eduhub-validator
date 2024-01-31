(ns nl.surf.eduhub-validator.main
  (:require [clojure.java.io :as io]
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

(defn version
  "Return app version"
  []
  (when-let [r (io/resource "nl/surf/eduhub_validator/version.txt")]
    (string/trim (slurp r))))


(defn -main
  [& args]
  (let [{:keys [errors summary options]} (parse-opts args cli-options)]
    (when (:print-version? options)
      (println (version))
      (System/exit 0))
    (when (seq errors)
      (run! println errors)
      (println summary)
      (when included-profiles
        (println "\nBuiltin profiles:")
        (run! #(println " - " %) included-profiles))
      (System/exit 1))
    (apie/main options)))
