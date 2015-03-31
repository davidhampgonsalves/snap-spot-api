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

(def create-param-validations [:id :secret :start :duration]) 
(defn create [req]
  "creates a new trip and returns the secret which is required for updating."
  (let [trip (params->trip (:params req))
        errors (trip/validate trip)]
    (if (nil? errors)
      (do (trip/persist trip)
        (json/write-str {:secret (:secret trip)}))
      (helper/error-response errors))))

(def update-param-validations [:id :secret :duration])

(comment "enforce duration range")
(defn update [req]
  "update trip duration(the only updatable attr)"
   (let [p (:params req)
        errors (b/validate p update-param-validations)]
     (if-let [trip (trip/fetch (:id p))]
       (do
         (trip/update (assoc trip :duration (:duration p)))
         (helper/success-response "trip updated"))         
       (helper/error-response "trip not found"))))
