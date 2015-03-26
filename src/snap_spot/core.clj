(ns snap-spot.core
  (:gen-class)
  (:use 
    [org.httpkit.server]
    [compojure.route :as route :only [files not-found]]
    [compojure.handler :only [site]] ; form, query params decode; cookie; session, etc
    [ring.middleware.reload :as reload]
    [compojure.core :only [defroutes GET POST DELETE ANY context]])
  (:require 
    [taoensso.carmine :as car :refer (wcar)]))

(def redis1-conn {:pool {} :spec {:host "localhost" :port 6379}})
(defmacro wcar* [& body] `(car/wcar redis1-conn ~@body))

(defn subscribe-position [req]
  (def params (:params req))
  (def channel-id (:id params))
  (with-channel req channel
    (println "are we connected? " (open? channel))
    (def position-listener
      (car/with-new-pubsub-listener (:spec redis1-conn)
        {channel-id (fn handle-position-event [[msg-type channel-id position]]
                      (when (= "message" msg-type)
                        (send! channel position)
                        (println "position update " position " recieved for " channel-id)))}
        (car/subscribe channel-id)))

    (on-close channel (fn [status] 
                        (println "channel closed: " status) 
                        (car/close-listener position-listener)))))

(defn update-position [req] 
  (def params (:params req))
  (wcar* (car/publish (:id params) [(:lat params) (:lon params)]))

  "Position updated.")

(defroutes all-routes
  (GET "/:id" [] "HOME!")
  (GET "/position/update/:id" [] update-position)
  (GET "/position/subscribe/:id" [] subscribe-position)
  (route/not-found "<p>BORK!</p>")) ;; all other, return 404

(defonce server (atom nil))
(defn in-dev? [args] true)

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [& args] ;; entry point, lein run will pick up and start from here
  (let [routes (if (in-dev? args)
                  (reload/wrap-reload (site #'all-routes)) ;; only reload when dev
                  (site all-routes))]
    (println "starting server on 9000")
    (reset! server (run-server routes {:port 9000}))))
