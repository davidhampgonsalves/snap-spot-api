(ns snap-spot.controllers.trip
  (:use 
    [org.httpkit.server]
    [compojure.route :as route :only [files not-found]]
    [compojure.core :only [defroutes GET POST DELETE ANY context]])
  (:require 
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
      (if-let [existing-trip (trip/fetch (:id trip))] 
        (helper/error-response "trip already exists.")
        (do (trip/create trip)
            (info "trip " (trip/redis-key trip) " was created.")
            (helper/success-response-with-data {:secret (:secret trip)} "trip created.")))
      (helper/error-response errors))))

(v/defvalidator duration-range-validator
  "validate duration range is valid"
  {:default-message-format "duration must be between 0-120"}
  [duration]
  (and (>= duration 0) (<= duration 120)))

(def update-param-validations {:id [v/required v/number] 
                               :secret [v/required]
                               :duration [v/required v/number duration-range-validator]})

(defn update [req]
  "update trip(duration) after validating secret"
  (let [p (:params req)
        errors (first (b/validate p update-param-validations))]
    (if-not (nil? errors)
      (helper/error-response errors)
      (let [existing-trip (trip/fetch (:id p))]
        (try
          (trip/valid-or-throw existing-trip) 
          (trip/valid-or-throw p)
          (trip/update p) 
          (info "trip " (trip/redis-key p) " was updated.")
          (helper/success-response "trip updated")
          (catch Exception e (helper/error-response (.getMessage e))))))))

