(ns upload_validate_java.config
  ( :require[noir.io :as io]
            [noir.response :as response]
            [noir.util.middleware :refer [app-handler]]
            [ring.util.response :refer [file-response]]
            [clj-time.core :as t]
            [clojure.java.io :as java_io]
            [clojure.data.json :as json]))

(use 'clojure.java.io)
(use 'clojure.string)

;; read/write config file

(def adress "config/config.json")

(defn json_write [string]
  (json/write-str string :key-fn name) )

(defn json_read [string]
  (json/read-str string :key-fn keyword))

(defn convertRawJsonConfig [file_as_string]
  (json_read file_as_string))

(defn readJsonConfigFile [adress]
  (slurp (do (java_io/resource adress))))

(defn writeJsonConfigFile [adress content]
  (spit (do (java_io/resource adress)) (json_write  content)))

;;(def config (json_read (readJsonConfigFile adress)))

(defn config []  (json_read (readJsonConfigFile adress)))

;; merge the loaded and via web interface new added values (remove also with post send :__anti-forgery-token)

(defn save_config [args]
  (writeJsonConfigFile adress ( merge (config)  (dissoc args :__anti-forgery-token)) ) )

(defn get_config [args]
  ( merge (config)  (dissoc args :__anti-forgery-token)))

