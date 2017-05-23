(ns luminus-upload-files.run
  (:require [ring.adapter.jetty :as jetty])
  (:require [luminus-upload-files.handler :as handler])

(:gen-class))

(defn -main [& args]
          (jetty/run-jetty #'handler/app {:port 3000}))
