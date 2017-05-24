(ns upload_validate_java.core
  (:require [ring.adapter.jetty :as jetty])
  (:require [upload_validate_java.handler :as handler])

(:gen-class))

(defn -main [& args]

          (jetty/run-jetty #'handler/app {:port 3000}))
