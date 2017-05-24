(ns upload_validate_java.handler
  (:use compojure.core)
  (:require [upload_validate_java.layout :as layout]
            [noir.io :as io]
            [noir.response :as response]
            [noir.util.middleware :refer [app-handler]]
            [ring.util.response :refer [file-response]]
            [upload_validate_java.validate :refer :all]
            [clj-time.core :as t]
            ))



(defroutes home-routes
  (GET "/" []
        (layout/render "index.html" {:date (t/today)}))
  (GET "/upload" []
       (layout/render "upload.html"))

  (POST "/upload" [file]
       ;; (str (validateAll file resource-path))
         (layout/render "validation.html" {:items (validateAll file resource-path)}));;layout/render "validation.html"

  (GET "/files/:filename" [filename]
       (file-response (str resource-path filename))))



(def app (app-handler

          ;; add your application routes here
          [home-routes]))
