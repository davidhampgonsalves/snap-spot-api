(ns snap-spot.controllers.position
  (:use 
    [org.httpkit.server]
    [compojure.route :as route :only [files not-found]]
    [compojure.core :only [defroutes GET POST DELETE ANY context]])
  (:require 
    [snap-spot.redis :as redis]
    [taoensso.carmine :as car :refer (wcar)]
    [me.shenfeng.mustache :as mustache]))

(defn subscribe [req]
  "subscribe via websocket to position events, driven by redis subpub"
  (def params (:params req))
  (def channel-id (:id params))
  (with-channel req channel
    (println "are we connected? " (open? channel))
    (def position-pubsub-listener
      (car/with-new-pubsub-listener (:spec redis/redis1-conn)
        {channel-id (fn handle-position-event [[msg-type channel-id position]]
                      (when (= "message" msg-type)
                        (send! channel position)
                        (println "position update " position " recieved for " channel-id)))}
        (car/subscribe channel-id)))

    (on-close channel (fn [status] 
                        (println "channel closed: " status) 
                        (car/close-listener position-pubsub-listener)))))

(defn update [req] 
  "update position for trip"
  (def params (:params req))
  (redis/wcar* (car/publish (:id params) [(:lat params) (:lon params)]))
  "Position updated.")
