(ns upload_validate_java.validate
  ( :require [upload_validate_java.layout :as layout]
              [upload_validate_java.config :refer :all]
            [noir.io :as io]
            [noir.response :as response]
            [noir.util.middleware :refer [app-handler]]
            [ring.util.response :refer [file-response]]
            [clj-time.core :as t]
            [clojure.java.io :as java_io]
            [clojure.data.json :as json]))


(use 'clojure.java.io)
(use 'clojure.string)

;; for internal use of some functions
(def text '())
(def validationMessage '())

;; uploaded file to validate dir
(def resource-path "/tmp/")

(defn extractConfigProperty [propertyType configMap]
  "iterate over a vector of maps and return the one who contains the propertyType (String) as lazy-sequenz"
  (for [x configMap] (if( = (get x :type) propertyType)  x ) ))


(defn getConfigProperty [propertyType configMap]
  "get a lazy-sequenz and convert it to a map. Only used with extractConfigProperty"
  (into {} (extractConfigProperty propertyType configMap)))

;; to access a specific property in the json config file
;; use (get (getConfigProperty "PROPERTY-NAME" (get (config) :items)) :KEY)


(defn getText [file resource-path]
  "Open the file and encoding it as text with special characters"
  (slurp (str resource-path (:filename file)) :encoding "ISO-8859-1"))

(defn textAsLines [file resource-path]
  "To order the code in sensable blocks (should be variable too to fit e.g. python code) "
  (concat text (split (getText file resource-path ) #";")))

(defn removeAllWhiteSpaces [text]
  "To make it easier to find char sequences"
  (clojure.string/replace text #"\s" ""))

(defn createRegExFromString [string]
  "To get a matching pattern uses java regex"
  (re-pattern(java.util.regex.Pattern/quote string)))


(defn splitFileIntoNameAndType [file]
  "seperate the file name into name and type e.g. test.java --> (\"test\" , \"java\")"
  (split (:filename file) #"\." ))

;; validateFileName and validateFileType are used to check the file for its name and type bevor uploading it to reduce traffic
(defn validateFileName [file]
  "first get config map with right type then get the name field of this map an compare it with the file name"
    (= ( first (splitFileIntoNameAndType file))(get (getConfigProperty "file-name" (get (config) :items)) :name))) ;; (get (config) :items) --> get the vector of all property elements as vector



(defn validateFileType [file]
    "first get config map with right type then get the name field of this map an compare it with the file type"
  (= ( second (splitFileIntoNameAndType file)) (get (getConfigProperty "file-type" (get (config) :items)) :name)))

(defn validatePackageName [file resource-path]
  "The package name / namespace get checked this should be the first statement"
      (and (= (str (first(split (first (textAsLines file resource-path )) #" ")))(get (getConfigProperty "package" (get (config) :items)) :name))
         (= (str (second(split (first (textAsLines file resource-path )) #" "))) (get (getConfigProperty "packageName" (get (config) :items)) :name) ))
  )


;;entry point check
(defn validateContainsMainFunction [file resource-path]
  "This search for the entry point a.k.a main function"
  (boolean (re-find (createRegExFromString (removeAllWhiteSpaces (get (getConfigProperty "entryPoint" (get (config) :items)) :name)))
                    (removeAllWhiteSpaces (getText file resource-path)))))

(defn validateFileExsits [file resource-path]
  "After the upload of the file. This function checks if the file is realy there"
  (.exists (clojure.java.io/as-file (str resource-path (:filename file)))))

(defn validateAll [file resource-path]
  "This is the main validator part. Firstly the name and type of the file will be checkt, then the file uploaded and then all test could run"
    (concat  (if(validateFileName file)
               (conj validationMessage  (get (getConfigProperty "file-name" (get (config) :items)) :success))
               (conj validationMessage  (get (getConfigProperty "file-name" (get (config) :items)) :fail)))

             (if(validateFileType file)
               (conj validationMessage  (get (getConfigProperty "file-type" (get (config) :items)) :success))
               (conj validationMessage  (get (getConfigProperty "file-type" (get (config) :items)) :fail)))

             (if(and (validateFileName file)(validateFileType file) )
               (concat
                  (io/upload-file resource-path file)

                  (if (validateFileExsits file resource-path)
                    (concat (conj validationMessage  "file upload successfull")
                            (if (validatePackageName file resource-path)
                              (conj validationMessage  (get (getConfigProperty "packageName" (get (config) :items)) :success))
                              (conj validationMessage  (get (getConfigProperty "packageName" (get (config) :items)) :fail)))
                            (if (validateContainsMainFunction file resource-path)
                                (conj validationMessage (get (getConfigProperty "entryPoint" (get (config) :items)) :success))
                                (conj validationMessage (get (getConfigProperty "entryPoint" (get (config) :items)) :fail)))
                    )
                    (conj validationMessage "file upload failed"))

                 ))
             )
)



