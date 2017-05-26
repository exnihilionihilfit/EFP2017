(ns upload_validate_java.validate
  (:require [upload_validate_java.layout :as layout]
            [noir.io :as io]
            [noir.response :as response]
            [noir.util.middleware :refer [app-handler]]
            [ring.util.response :refer [file-response]]

            [clj-time.core :as t]))



(def file-name "FileServer")
(def file-type "java")
(def importOrder "import")
(def package "package")
(def packageName "var.mom.jms.file")
(def entryPoint "static void main(String [] args)")

(def text '())
(def validationMessage '())

;; uploaded file dir
(def resource-path "/tmp/")

(use 'clojure.java.io)
(use 'clojure.string)



(defn getText [file resource-path]
  (slurp (str resource-path (:filename file)) :encoding "ISO-8859-1"))

(defn textAsLines [file resource-path]
  (concat text (split (getText file resource-path ) #";")))

(defn removeAllWhiteSpaces [text]
  ())

(defn splitFileIntoNameAndType [file]
  (split (:filename file) #"\." ))

(defn validateFileName [file]
  (= ( first (splitFileIntoNameAndType file)) file-name))

(defn validateFileType [file]
  (= ( second (splitFileIntoNameAndType file)) file-type))

(defn validatePackageName [file resource-path]

      (and (= (str (first(split (first (textAsLines file resource-path )) #" "))) package)
         (= (str (second(split (first (textAsLines file resource-path )) #" "))) packageName))
  )
;;entry point check

(defn validateContainsMainFunction [file resource-path]
  (boolean (re-find (re-pattern(java.util.regex.Pattern/quote (clojure.string/replace entryPoint #"\s" "")))
                    (clojure.string/replace (getText file resource-path) #"\s" ""))))

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



