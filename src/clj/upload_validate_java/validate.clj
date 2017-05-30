(ns upload_validate_java.validate
  ( :require [upload_validate_java.layout :as layout]
            [noir.io :as io]
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
  (json/write-str string))

(defn json_read [string]
  (json/read-str string :key-fn keyword))

(defn convertRawJsonConfig [file_as_string]
  (json_read file_as_string))

(defn readJsonConfigFile [adress]
  (slurp (do (java_io/resource adress))))

(def config (json_read (readJsonConfigFile adress)))

;;


(def text '())
(def validationMessage '())

;; uploaded file to validate dir
(def resource-path "/tmp/")


(defn getText [file resource-path]
  (slurp (str resource-path (:filename file)) :encoding "ISO-8859-1"))

(defn textAsLines [file resource-path]
  (concat text (split (getText file resource-path ) #";")))

(defn removeAllWhiteSpaces [text]
  (clojure.string/replace text #"\s" ""))

(defn createRegExFromString [string]
  (re-pattern(java.util.regex.Pattern/quote string)))


(defn splitFileIntoNameAndType [file]
  (split (:filename file) #"\." ))

(defn validateFileName [file]
  (= ( first (splitFileIntoNameAndType file)) (get config :file-name)))

(defn validateFileType [file]
  (= ( second (splitFileIntoNameAndType file)) (get config :file-type)))

(defn validatePackageName [file resource-path]

      (and (= (str (first(split (first (textAsLines file resource-path )) #" "))) (get config :package))
         (= (str (second(split (first (textAsLines file resource-path )) #" "))) (get config :packageName)))
  )
;;entry point check


(defn validateContainsMainFunction [file resource-path]
  (boolean (re-find (createRegExFromString (removeAllWhiteSpaces (get config :entryPoint)))
                    (removeAllWhiteSpaces (getText file resource-path)))))

(defn validateFileExsits [file resource-path]
  (.exists (clojure.java.io/as-file (str resource-path (:filename file)))))

(defn validateAll [file resource-path]

    (concat  (if(validateFileName file)(conj validationMessage  "file name okay")(conj validationMessage "file name invalide"))
             (if(validateFileType file)(conj validationMessage  "file type okay")(conj validationMessage "file type invalide"))

             (if(and (validateFileName file)(validateFileType file) )
               (concat
                  (io/upload-file resource-path file)

                  (if (validateFileExsits file resource-path)
                    (concat (conj validationMessage  "file upload successfull")
                            (if (validatePackageName file resource-path)
                              (conj validationMessage  "package name valide")
                              (conj validationMessage "package name invalide"))
                            (if (validateContainsMainFunction file resource-path)
                                (conj validationMessage  "contains entry point")
                                (conj validationMessage "no entry point found"))
                    )
                    (conj validationMessage "file upload failed"))

                 ))
             )
)



