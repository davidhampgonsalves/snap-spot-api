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
   :remaining-minutes (helper/str->number (:remaining-minutes p)) 
   :secret (if (contains? p :secret) (:secret p) (helper/generate-uuid))
   :start (t/now)})

(defn create [req]
  "creates a new trip and returns the secret which is required for updating."
  (let [trip (params->trip (:params req))
        errs (trip/create trip)]
    (if (empty? errs)
      (helper/success-response "trip created." :data {:secret (:secret trip)})
      (helper/error-response errs))))

(defn update [req]
  "update trip(duration) after validating secret"
  (let [errs (trip/update (:params req))]
    (if (empty? errs)
      (helper/success-response "trip updated")
      (helper/error-response errs))))
