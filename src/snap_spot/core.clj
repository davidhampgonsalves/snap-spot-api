(ns snap-spot.core
  (:gen-class)
  (:use 
    [org.httpkit.server]
    [compojure.route :as route :only [files not-found]]
    [compojure.handler :only [site]] ; form, query params decode; cookie; session, etc
    [ring.middleware.reload :as reload]
    [compojure.core :only [defroutes GET POST DELETE ANY context]])
  (:require 
    [taoensso.carmine :as car :refer (wcar)]
    [me.shenfeng.mustache :as mustache]
    [snap-spot.controllers
      (position :as position)]))

(mustache/deftemplate index-template (slurp "templates/index.tpl"))

(defroutes all-routes
  (context "/position" [] 
    (GET "/update/:id" [] position/update)
    (GET "/subscribe/:id" [] position/subscribe))
  (GET "/:id" [] (index-template {:title "TItLE"}))
  (route/not-found "<p>BORK!</p>")) ;; all other, return 404

(defn in-dev? [args] true)

(defonce server (atom nil))
(defn stop-server []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [& args]
  (let [routes (if (in-dev? args)
                  (reload/wrap-reload (site #'all-routes)) ;; only reload when dev
                  (site all-routes))]
    (println "starting server on 9000")
    (reset! server (run-server routes {:port 9000}))))
