(ns nl.surf.eduhub-validator.main
  (:require [nl.jomco.apie.main :as apie]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io]))

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

(defn -main
  [& args]
  (let [{:keys [errors summary options]} (parse-opts args cli-options)]
    (when (seq errors)
      (run! println errors)
      (println summary)
      (when included-profiles
        (println "\nBuiltin profiles:")
        (run! #(println " - " %) included-profiles))
      (System/exit 1))
    (apie/main options)))
