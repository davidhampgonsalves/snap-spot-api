(ns snap-spot.controllers.position
  (:use 
    [org.httpkit.server]
    [compojure.route :as route :only [files not-found]]
    [compojure.core :only [defroutes GET POST DELETE ANY context]])
  (:require 
    [snap-spot.redis :as redis]
    [taoensso.carmine :as car :refer (wcar)]
    [taoensso.timbre :as timbre]
    [bouncer.core :as b]
    [bouncer.validators :as v]
    [clojure.data.json :as json]
    [me.shenfeng.mustache :as mustache]
    [snap-spot.helper :as helper]
    [snap-spot.models 
     (position :as position)
     (trip :as trip)]))

(timbre/refer-timbre)

(defn send-position [channel position]
  "send postion to websocket channel"
  (send! channel (json/write-str position)))

(defn send-past-positions [channel params]
  "send all past positions to websocket channel"
  (def positions (position/fetch-all params))
  (doseq [p (sort-by :instant > positions)] (send-position channel p)))

(defn params->position [params]
  "turn url params into a position"
  (def attrs [:lat :lon :order])
  (-> params 
    (select-keys attrs)
    (helper/values->numbers attrs)))

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

(defn save-new-position [trip position]
  (redis/wcar*
    (if (> (trip/seconds-since-update trip) 1)
      (position/add trip position)
      (info "position not writen to db since last update was too recent"))
    (car/publish (:id trip) position)))

(def add-param-validations (helper/generate-required-validations [:id :secret :lat :lon :order])) 
(defn add [req] 
  "add position to a trip"
  (let [p (:params req)
        position (params->position p)
        errors (helper/validate-all [[p add-param-validations] 
                                     [position position/validations]])]
    (if errors
      (helper/error-response errors) 
      (let [trip (trip/fetch (:id p))]
        (try
          (trip/valid-or-throw p)
          (save-new-position trip position)
          (helper/success-response "position added")
          (catch Exception e (helper/error-response (.getMessage e))))))))