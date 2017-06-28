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

;; counter
(def counter (atom -1))

(defn next-value []
  "increments the counter"
(swap! counter inc))

;; uploaded file to validate dir
(def resource-path "/tmp/")


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

(defn formatValidationMessage [string status]
  "Concat Validation Message with HTML Tags"
  (if(= status "success")
    (str string "<span> <i class='fa fa-check'></i></span>")
    (str string "<span> <i class='fa fa-times'></i></span>"))
  )

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

       (= (str (second(split (first (textAsLines file resource-path )) #" "))) (get (getConfigProperty "packageName" (get (config) :items)) :name) )
  )

(defn validatePackage [file resource-path]
  "The package name / namespace get checked this should be the first statement"
 (= (str (first(split (first (textAsLines file resource-path )) #" ")))(get (getConfigProperty "package" (get (config) :items)) :name))
)

;;entry point check
(defn validateContainsMainFunction [file resource-path]
  "This search for the entry point a.k.a main function"
  (boolean (re-find (createRegExFromString (removeAllWhiteSpaces (get (getConfigProperty "entryPoint" (get (config) :items)) :name)))
                    (removeAllWhiteSpaces (getText file resource-path)))))

(defn validateFileExists [file resource-path]
  "After the upload of the file. This function checks if the file is realy there"
  (.exists (clojure.java.io/as-file (str resource-path (:filename file)))))

(defn validateAll [file resource-path]
  "This is the main validator part. Firstly the name and type of the file will be checkt, then the file uploaded and then all test could run
  It returns a string with all messages of success and failure"
    ( merge {:totalValidations (-  (count (get (config) :items)) (reset! counter 0) 1)}
           (if(validateFileName file)
               (hash-map (keyword  (get (getConfigProperty "file-name" (get (config) :items)) :success)) true, :numberOfValid (next-value))
               (hash-map (keyword  (get (getConfigProperty "file-name" (get (config) :items)) :fail)) false))

             (if(validateFileType file)
               (hash-map (keyword  (get (getConfigProperty "file-type" (get (config) :items)) :success)) true, :numberOfValid (next-value))
               (hash-map (keyword  (get (getConfigProperty "file-type" (get (config) :items)) :fail)) false))

             (if(and (validateFileName file)(validateFileType file) )

                  (merge (io/upload-file resource-path file)
                    (if (validateFileExists  file resource-path)
                    ( merge (hash-map (keyword "file upload successfull") true)
                            (if (validatePackage file resource-path)
                              (hash-map (keyword  (get (getConfigProperty "package" (get (config) :items)) :success)) true , :numberOfValid (next-value))
                              (hash-map (keyword  (get (getConfigProperty "package" (get (config) :items)) :fail)) false ))
                            (if (validatePackageName file resource-path)
                              (hash-map (keyword  (get (getConfigProperty "packageName" (get (config) :items)) :success)) true , :numberOfValid (next-value))
                              (hash-map (keyword  (get (getConfigProperty "packageName" (get (config) :items)) :fail)) false ))
                            (if (validateContainsMainFunction file resource-path)
                                (hash-map (keyword  (get (getConfigProperty "entryPoint" (get (config) :items)) :success)) true , :numberOfValid (next-value))
                                (hash-map (keyword  (get (getConfigProperty "entryPoint" (get (config) :items)) :fail)) false))
                    )
                    (hash-map (keyword "file upload failed") false)))

                 )

      )
)

(defn validationPercentage[validation]
 (if(and (contains? validation :numberOfValid) (contains? validation :totalValidations) )
   (* ( /  (get  validation :numberOfValid) (get  validation :totalValidations)) 100)
   (str "0")))

(defn filterValidateAll[validation]
  "To filter out the validation counters and total count for display onto html page"
  (if(and (contains? validation :numberOfValid) (contains? validation :totalValidations) )
    (dissoc validation :numberOfValid :totalValidations)
      (hash-map (keyword "please choose a file") false))
)
