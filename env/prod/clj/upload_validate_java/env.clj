(ns upload-validate-java.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[upload_validate_java started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[upload_validate_java has shut down successfully]=-"))
   :middleware identity})
