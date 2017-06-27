(ns upload_validate_java.config
  ( :require
            [noir.io :as io]
    [noir.response :as response]
    [noir.util.middleware :refer [app-handler]]
    [ring.util.response :refer [file-response]]
    [clj-time.core :as t]
    [clojure.java.io :as java_io]
    [clojure.data.json :as json]))

(use 'clojure.java.io)
(use 'clojure.string)

;; to access a specific property in the json config file
;; use (get (getConfigProperty "PROPERTY-NAME" (get (config) :items)) :KEY)

(defn extractConfigProperty [propertyType configMap]
  "iterate over a vector of maps and return the one who contains the propertyType (String) as lazy-sequenz"
  (for [x configMap] (if( = (get x :type) propertyType)  x ) ))

(defn getConfigProperty [propertyType configMap]
  "get a lazy-sequenz and convert it to a map. Only used with extractConfigProperty"
  (into {} (extractConfigProperty propertyType configMap)))



;; regex

(defn splitKeys [string]
  (split string #"\:" ))

;; iterate over each form entry send via post message
;; get primary key (get (  splitKeys (str (get x 0)))  0)
;; get config item with this key
;; iterate over each item field and change to new value
;; save the changes

(defn updateConfigProperty [entry newEntries]
  "iterate over a vector of maps and return the one who contains the propertyType (String) as lazy-sequenz"
  (into {}
    (for [newEntry newEntries]
     (let[itemKey (  splitKeys (str (get newEntry 0)))]
       (if( =  (get itemKey 0) (get entry :type))
       (merge {} (hash-map (keyword (get itemKey 1)) (val newEntry)))

       )) ) ))


;; read/write config file

(def adress "config/config.json")

(defn json_write [string]
  "Converts a string into json formated string"
  (json/write-str string :key-fn name))

(defn json_read [string]
  "Converts a json formated string into a map"
  (json/read-str string :key-fn keyword))

(defn convertRawJsonConfig [file_as_string]
  "Gets the raw file string and converted it into a map"
  (json_read file_as_string))

(defn readJsonConfigFile [adress]
  "Opens and read the config file by given adress"
  (slurp (do (java_io/resource adress))))

(defn writeJsonConfigFile [adress content]
  "Convert map into a json string, then writes it into a file located by adress"
  (spit (do (java_io/resource adress)) (json_write  content)))

(defn config []
  "Reads the config files resource/config/config.json by default. Converts it into a map"
  (json_read (readJsonConfigFile adress)))



(defn extractPOSTMessage [newEntries]
  "x is the type field of the json object entry. Get the key from x remove the colon and search for the entry in
  the config file. Then replace the entry with the new value."
  (let [configEntries (get (config) :items)](
       hash-map :items
    ( for [entry configEntries] (updateConfigProperty entry newEntries) )
    )
  )
  )

(defn save_config [newEntries]
  "merge the loaded and via web interface new added values (remove also with post send :__anti-forgery-token)"
  (writeJsonConfigFile adress (extractPOSTMessage(dissoc newEntries :__anti-forgery-token)) ))


(defn get_config []
  ""
    (get (config) :items))


