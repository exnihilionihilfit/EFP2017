(defproject luminus-upload-files "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [lib-noir "0.8.4"]
                 [ring-server "0.4.0"]
                 [com.taoensso/timbre "3.2.1"]
                 [com.taoensso/tower "2.0.2"]
                 [markdown-clj "0.9.47"]
                 [environ "0.5.0"]
                 [im.chit/cronj "1.0.1"]
                 [noir-exception "0.2.2"]
                 [clj-time "0.13.0"]
                 [lein-ancient "0.6.10"]
                 [selmer "1.10.7"]
                 [ring-webjars "0.2.0"]
                 [org.webjars/bootstrap "4.0.0-alpha.5"]
                 [org.webjars/font-awesome "4.7.0"]]

  :repl-options {:init-ns luminus-upload-files.repl}
  :jvm-opts ["-server"]
  :plugins [[lein-ring "0.12.0"]
            [lein-environ "0.5.0"]]
  :ring {:handler luminus-upload-files.handler/app
         ;; :init    luminus-upload-files.handler/init
         ;; :destroy luminus-upload-files.handler/destroy
         }
:resource-paths ["resources"]
  :main luminus-upload-files.run
  :profiles
  {:uberjar {:aot :all :main luminus-upload-files.run}
   :production {:ring {:open-browser? false
                       :stacktraces?  false
                       :auto-reload?  false}}
   :dev {:dependencies [[ring-mock "0.1.5"]
                        [ring/ring-devel "1.3.0"]
                        [pjstadig/humane-test-output "0.6.0"]]
         :injections [(require 'pjstadig.humane-test-output)
                      (pjstadig.humane-test-output/activate!)]
         :env {:dev true}}}
  :min-lein-version "2.0.0")
