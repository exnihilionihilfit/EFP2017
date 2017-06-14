(ns upload_validate_java.core
  (:require [ring.adapter.jetty :as jetty])
  (:require [upload_validate_java.handler :as handler])

(:gen-class))

(defn -main [& args]
  "Starts the app with jetty and calls the handler in handler.clj. The application is reachable by local ip adress and port 3000"
          (jetty/run-jetty #'handler/app {:port 3000}))
