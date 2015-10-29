(ns snap-spot.core
  (:gen-class)
  (:use 
    [org.httpkit.server]
    [compojure.route :as route :only [files not-found]]
    [ring.middleware.reload :as reload]
    [ring.middleware.defaults :refer :all]
    [ring.middleware.json :refer [wrap-json-params]]
    [compojure.core :only [defroutes GET POST PUT DELETE context]])
  (:require 
    [taoensso.carmine :as car :refer (wcar)]
    [taoensso.timbre :as timbre]
    [me.shenfeng.mustache :as mustache]
    [snap-spot.controllers
      (position :as position)
      (trip :as trip)]))

(timbre/set-level! :info)
(mustache/deftemplate index-template (slurp "templates/index.tpl"))

(defroutes all-routes
  (context "/position" [] 
    (POST "/add/:id" [] position/add)
    (GET "/subscribe/:id" [] position/subscribe))
  (context "/trip" []
    (POST "/create/:id" [] trip/create)
    (PUT "/update/:id" [] trip/update))
  (GET "/:id" [] (index-template {:title "SS"}))
  (route/not-found "<p>BORK!</p>")) ;; all other, return 404

(defn in-dev? [args] true)

(defonce server (atom nil))
(defn stop-server []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(def site 
  (-> (wrap-defaults all-routes (assoc-in api-defaults [:params] {:keywordize true, :urlencoded true}))
    wrap-json-params))

(defn -main [& args]
  (let [routes (if (in-dev? args)
                  (reload/wrap-reload site) ;; only reload when dev
                  site)]
    (println "starting server on 9000")
    (reset! server (run-server routes {:port 9000}))))
