(ns upload-validate-java.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [upload-validate-java.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[upload_validate_java started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[upload_validate_java has shut down successfully]=-"))
   :middleware wrap-dev})
