(ns upload_validate_java.handler
  (:use compojure.core)
  (:require [upload_validate_java.layout :as layout]
            [upload_validate_java.config :refer :all]
            [upload_validate_java.validate :refer :all]
            [noir.io :as io]
            [noir.response :as response]
            [noir.util.middleware :refer [app-handler]]
            [ring.util.response :refer [file-response]]
            [clj-time.core :as t]
            [ring.middleware.webjars :refer [wrap-webjars]]
            ))



(defroutes home-routes
  (selmer.filters/add-filter! :true? true?)
  (selmer.filters/add-filter! :empty? empty?)
  (selmer.filters/add-filter! :key key)
  (selmer.filters/add-filter! :val val)
  (selmer.filters/add-filter! :deleteColon #(clojure.string/replace % #":" ""))

  (GET "/" []
       (layout/render "index.html" {:default "true"}))

  (GET "/date" []
       (layout/render "index.html" {:about "true"}))

  (GET "/upload" []
       (layout/render "index.html" {:upload "true" :config (get_config) }))

  (GET "/edit_config" []
       (layout/render "index.html" {:edit_config "true" :config (config)} true))


  (POST "/save_config" [& args]
        (layout/render "index.html" {:config (get_config) :edit_config "true"  :do_save (save_config args) }))

  (POST "/upload" [file]
        (layout/render "index.html" {:items (filterValidateAll(validateAll file resource-path)) :validationPercentage (validationPercentage (validateAll file resource-path)) :validate "true"}));;layout/render "validation.html"

  (GET "/files/:filename" [filename]
       (file-response (str resource-path filename))))


(def app ( wrap-webjars (app-handler

                          ;; added application routes
                          [home-routes])))
