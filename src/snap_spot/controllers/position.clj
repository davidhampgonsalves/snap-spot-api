(ns snap-spot.controllers.position
  (:use 
    [org.httpkit.server]
    [compojure.route :as route :only [files not-found]]
    [compojure.core :only [defroutes GET POST DELETE ANY context]])
  (:require 
    [snap-spot.redis :as redis]
    [taoensso.carmine :as car :refer (wcar)]
    [taoensso.timbre :as timbre]
    [clojure.data.json :as json]
    [me.shenfeng.mustache :as mustache]))

(timbre/refer-timbre)

(defn get-position-key [params] (str (:id params) "-positions"))

(defn send-position [channel position]
  "send postion to websocket channel"
  (send! channel (json/write-str (zipmap [:lat :lon :instant] position))))

(defn send-past-positions [channel params]
  "send all past positions to websocket channel"
  (def positions (redis/wcar* (car/lrange (get-position-key params) 0 -1)))
  (doseq [p (reverse positions)] (send-position channel p)))

(defn subscribe-to-redis [channel trip-id]
  "subscribe to redis sub/pub for a trip and push to websocket channel"
  (def position-pubsub-listener
    (car/with-new-pubsub-listener (:spec redis/redis1-conn)
      {trip-id (fn handle-position-event [[msg-type trip-id position]]
                    (when (= "message" msg-type)
                      (send-position channel position)
                      (info "position update " position " recieved for " trip-id)))}
      (car/subscribe trip-id)))
  (on-close channel (fn [status] (car/close-listener position-pubsub-listener))))

(defn subscribe [req]
  "subscribe via websocket to position events provided by redis subpub"
  (def params (:params req))
  (with-channel req channel
    (subscribe-to-redis channel (:id params))
    (send-past-positions channel params)))

(defn update [req] 
  "update position for trip"
  (def params (:params req))
  (redis/wcar* 
    (if (car/get (:id params)) 
      (do
        (car/publish (:id params) (map params [:lat :lon :instant]))
        (car/lpush (get-position-key params) (select-keys params [:lat :lon :instant])))
      "BORK!(trip not found)"))
  "Position updated.")
