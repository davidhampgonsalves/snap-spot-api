(ns snap-spot.trip-test
  (:require [clojure.test :refer :all]
            [snap-spot.core :refer :all]
            [snap-spot.models.trip :as model]
            [snap-spot.helpers
              (test-helper :as test-helper)]
            [snap-spot.controllers.trip :as controller]
            [clojure.data.json :as json]))

(use-fixtures :each test-helper/stateless-redis-fixture)

(deftest test-params->trip
  (testing "params->trip"
    (is (= 1 (:remaining-minutes (controller/params->trip {:id "1" :remaining-minutes "1"}))))))

(deftest test-create
  (testing "controller/create valid"
    (def trip (controller/create {:params {:id 2 :remaining-minutes 30}}))
    (is (contains? (test-helper/response-json trip) "secret"))))

(deftest test-create-duplicate
  (testing "controller/create duplicate trip"
    (controller/create {:params {:id 2 :remaining-minutes 30}})
    (def error (controller/create {:params {:id 2 :remaining-minutes 30}}))
    (is (contains? (test-helper/response-json error) "errors"))))

(deftest test-create-bad-remaining-minutes
  (testing "controller/create bad remaining-minutes"
    (def error (controller/create {:params {:id 3 :remaining-minutes -30}}))
    (is (contains? (test-helper/response-json error) "errors"))))

(deftest test-create-no-remaining-minutes
  (testing "controller/create no remaining-minutes error test"
    (def error (controller/create {:params {:id 4}}))
    (is (contains? (test-helper/response-json error) "errors"))))

(deftest test-delete
  (testing "controller/delete"
    (def trip (test-helper/create-trip))
    (testing "- success"
      (let [res (controller/delete {:params {:id (:id trip) :secret (:secret trip)}})]
        (is (contains? (test-helper/response-json res) "success"))))
    (testing "- was deleted"
      (is (empty (model/fetch trip))))))

(deftest test-update
  (testing "controller/update remaining-minutes test"
    (def trip (test-helper/create-trip :id 5))
    (testing "- success"
      (let [res (controller/update {:params {:id 5 :secret (:secret trip) :remaining-minutes 120}})]
        (is (contains? (test-helper/response-json res) "success"))))

    (testing "- invalid secret test"
      (let [error (controller/update {:params {:id 5 :secret "abc" :remaining-minutes 120}})]
        (is (contains? (test-helper/response-json error) "errors"))))

    (testing "- too long"
      (let [error (controller/update {:params {:id 5 :secret (:secret trip) :remaining-minutes 130}})]
        (is (contains? (test-helper/response-json error) "errors"))))

    (testing "- negative"
      (let [error (controller/update {:params {:id 5 :secret (:secret trip) :remaining-minutes -1}})]
        (is (contains? (test-helper/response-json error) "errors"))))))

(comment deftest test-valid
  (testing "controller/errors"
    (let [trip (test-helper/create-trip)
          expired-trip (test-helper/create-trip :remaining-minutes 0)
          err-expired (model/update expired-trip)
          err-bad-secret (model/update (assoc trip :secret 1))
          err-does-not-exist (model/update (assoc trip :id 1))]
      (testing "trip not active"
        (is (thrown-with-msg? Exception #"does not exist" err-expired)))
      (testing "trip does not exist"
        (is (thrown-with-msg? Exception #"does not exist" err-does-not-exist))))))
