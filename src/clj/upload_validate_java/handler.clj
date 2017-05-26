(ns upload_validate_java.handler
  (:use compojure.core)
  (:require [upload_validate_java.layout :as layout]
            [noir.io :as io]
            [noir.response :as response]
            [noir.util.middleware :refer [app-handler]]
            [ring.util.response :refer [file-response]]
            [upload_validate_java.validate :refer :all]
            [clj-time.core :as t]
            [ring.middleware.webjars :refer [wrap-webjars]]
            ))


(defroutes home-routes
  (GET "/" []
        (layout/render "index.html" ))

    (GET "/date" []
        (layout/render "index.html" {:date (t/today)}))

  (GET "/upload" []
       (layout/render "index.html" {:upload "true"}))
    ;;   (layout/render "upload.html"))

  (POST "/upload" [file]
         (layout/render "index.html" {:items (validateAll file resource-path) :validate "true"}));;layout/render "validation.html"

  (GET "/files/:filename" [filename]
       (file-response (str resource-path filename))))



(def app ( wrap-webjars (app-handler

          ;; add your application routes here
          [home-routes])))
