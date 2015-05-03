(ns snap-spot.controllers.trip
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
    [clj-time.core :as t]
    [me.shenfeng.mustache :as mustache]
    [snap-spot.helper :as helper]
    [snap-spot.models 
     (trip :as trip)]))

(timbre/refer-timbre)

(defn params->trip [p]
  "build a trip map from request params"
  {:id (:id p) 
   :duration (helper/str->number (:duration p)) 
   :secret (helper/generate-uuid) 
   :start (t/now)})

(defn create [req]
  "creates a new trip and returns the secret which is required for updating."
  (let [trip (params->trip (:params req))
        errors (trip/validate trip)]
    (if (nil? errors)
      (do (trip/persist trip)
        (json/write-str {:secret (:secret trip)}))
      (helper/error-response errors))))

(v/defvalidator duration-range-validator
  {:default-message-format "duration must be between 0-120"}
  [duration]
  (and (>= duration 0) (<= duration 120)))

(def update-param-validations {:id [v/required] 
                               :secret [v/required]
                               :duration [v/required v/number duration-range-validator]})

(defn update [req]
  "update trip duration(the only updatable attr)"
   (let [p (:params req)
        errors (first (b/validate p update-param-validations))]
     (if (nil? errors)
       (if-let [trip (trip/fetch (:id p))]
         (if (= (:secret trip) (:secret p))
           (do
             (trip/update (assoc trip :duration (:duration p)))
             (helper/success-response "trip updated"))
           (helper/error-response "invalid secret")) 
         (helper/error-response "trip not found"))
       (helper/error-response errors))))
