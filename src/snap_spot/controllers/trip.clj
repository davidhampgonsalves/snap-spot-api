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
    [me.shenfeng.mustache :as mustache]))

(timbre/refer-timbre)

(defn create [req]
  "creates a new trip"
  (wcar* (car/set (:id params) (select-keys params [:duration :start-time]))))
