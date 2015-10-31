(ns snap-spot.helper
  (:require 
    [bouncer.core :as b]
    [bouncer.validators :as v]
    [clj-time.core :as t]
    [clojure.data.json :as json]))

(defn error-response [errors] 
  "create json error message"
  {:status 400 
   :headers {"Content-Type" "application/json"} 
   :body (json/write-str {:errors errors})})

(defn success-response 
  [msg & {:keys [data] or {data (hash-map)}}] 
  "create json success message"
  {:status 200 
   :headers {"Content-Type" "application/json"} 
   :body (json/write-str (assoc data :success msg))})

(defn throw-exception [msg]
  (throw (Exception. msg)))

(defn generate-uuid []
  (str (java.util.UUID/randomUUID)))

(defn str->number [s]
  "convert string to number"
  (try (bigdec s) (catch Exception e s)))

(defn str->int [s]
  (if-let [number (str->number s)]
    (.intValue number)
    nil))

(defn values->numbers [m keys]
  (reduce (fn [r [k v]] (assoc r k (str->number v))) {} m))

(defn generate-required-validations [attrs]
  (reduce #(assoc %1 %2 [v/required]) {} attrs))

(defn validate-all [obj-validator-pairs]
  "call validate on each object/validator rule pairs sequentially and return first error set"
  (loop [o-v-pairs obj-validator-pairs]
    (let [o-v-pair (first o-v-pairs)
          errors (first (apply b/validate o-v-pair))]
      (if errors
        errors
        (if (not-empty o-v-pairs)
          (recur (rest o-v-pairs)))))))
