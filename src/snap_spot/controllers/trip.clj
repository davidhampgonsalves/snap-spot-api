(ns snap-spot.controllers.trip
  (:use 
    [org.httpkit.server]
    [compojure.route :as route :only [files not-found]]
    [compojure.core :only [defroutes GET POST DELETE ANY context]])
  (:require 
    [snap-spot.redis :as redis]
    [taoensso.carmine :as car :refer (wcar)]
    [taoensso.timbre :as timbre]
    [clojure.data.json :as json]
    [clj-time.core :as t]
    [me.shenfeng.mustache :as mustache]
    [snap-spot.helper :as helper]
    [snap-spot.models 
     (trip :as trip)]))

(timbre/refer-timbre)

(defn trip-from-params [p]
  "build a trip map from request params"
  (def duration (Integer/parseInt (:duration p)))
  {:id (:id p) 
   :duration duration 
   :secret (str (java.util.UUID/randomUUID))
   :start (t/now)})

(comment "TODO: validate trip params")
(defn create [req]
  "creates a new trip and returns the secret which is required for updating."
  (let [trip (trip-from-params (:params req))
        errors (trip/validate trip)]
    (if (nil? errors)
      (do (trip/persist trip)
        (json/write-str {:secret (:secret trip)}))
      (helper/error-response errors))))
