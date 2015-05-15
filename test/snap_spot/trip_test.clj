(ns snap-spot.trip-test
  (:require [clojure.test :refer :all]
            [snap-spot.core :refer :all]
            [snap-spot.models.trip :as model]
            [snap-spot.helpers
              (test-helper :as test-helper)
              (trip-helper :as helper)]
            [ring.mock.request :as mock]
            [snap-spot.controllers.trip :as controller]
            [clojure.data.json :as json]))

(use-fixtures :each test-helper/stateless-redis-fixture)

(deftest test-params->trip
  (testing "params->trip"
    (is (= 1M (:duration (controller/params->trip {:id "1" :duration "1"}))))))

(deftest test-create
  (testing "controller/create valid"
    (def trip (json/read-str (controller/create {:params {:id 2 :duration 30}})))
    (is (contains? trip "secret"))))

(deftest test-create-duplicate
  (testing "controller/create duplicate trip"
    (controller/create {:params {:id 2 :duration 30}})
    (def error (json/read-str (controller/create {:params {:id 2 :duration 30}})))
    (is (contains? error "error"))))

(deftest test-create-bad-duration
  (testing "controller/create bad duration"
    (def error (json/read-str (controller/create {:params {:id 3 :duration -30}})))
    (is (contains? error "error"))))

(deftest test-create-no-duration
  (testing "controller/create no duration error test"
    (def error (json/read-str (controller/create {:params {:id 4}})))
    (is (contains? error "error"))))

(deftest test-update
  (testing "controller/update invalid secret test"
    (def trip (json/read-str (controller/create {:params {:id 5 :duration 15}}) :key-fn keyword))
    (let [error (json/read-str (controller/update {:params {:id 5 :secret "abc" :duration -1}}))]
      (is (contains? error "errors")))))

(deftest test-update
  (testing "controller/update duration test"
    (def trip (json/read-str (controller/create {:params {:id 5 :duration 15}}) :key-fn keyword))

    (testing "- success"
      (let [res (json/read-str (controller/update {:params {:id 5 :secret (:secret trip) :duration 120}}))]
        (is (contains? res "success"))))

    (testing "- too long"
      (let [error (json/read-str (controller/update {:params {:id 5 :secret (:secret trip) :duration 130}}))]
        (is (contains? error "error"))))

    (testing "- negative"
      (let [error (json/read-str (controller/update {:params {:id 5 :secret (:secret trip) :duration -1}}))]
        (is (contains? error "error"))))))

(deftest test-valid
  (testing "controller/valid-or-throw"
    (let [trip (helper/create)
          trip-inactive (helper/create :duration (/ 1 60))
          trip-bad-secret (assoc trip :secret 1)
          trip-does-not-exist (assoc trip :id 1)]
      (testing "nil trip throws"
        (is (thrown-with-msg? Exception #"nil" (model/valid-or-throw nil))))
      (testing "trip invalid secret throws"
        (is (thrown-with-msg? Exception #"invalid secret" (model/valid-or-throw trip-bad-secret))))
      (testing "trip not active throws"
        (Thread/sleep 1200)
        (is (thrown-with-msg? Exception #"does not exist" (model/valid-or-throw trip-inactive))))
      (testing "trip does not exist"
        (is (thrown-with-msg? Exception #"does not exist" (model/valid-or-throw trip-does-not-exist)))))))
