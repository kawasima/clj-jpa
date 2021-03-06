(ns clj-jpa.entity-manager-test
  (:require [clj-jpa.core :refer :all]
            [clj-jpa.entity-manager :as em]
            [clj-jpa.query :as query]
            [clj-jpa.fixture :refer :all]
            [clojure.test :refer :all])
  (:import [cljjpa.model User Group]))


(defn em-setup [f]
  (with-entity-manager
    (with-transaction
      (let [user (em/merge User {:family-name "Kawashima"
                                  :last-name "Yoshitaka"
                                  :email-address "kawasima1016@gmail.com"
                                  :age (int 10)})]
        (em/merge Group {:name "group1"
                         :users [user]}))))
  (f))

(use-fixtures :once wrap-weld-setup)
(use-fixtures :each (join-fixtures [wrap-table-setup em-setup]))

(testing "Search API"
  (deftest entity-manager-creation
    (with-entity-manager
      (let [users (em/search User
                             :where (and (= :family-name "Kawashima")
                                         (= :last-name   "Yoshitaka")))]
        (is (= (count users) 1))
        (is (= (get-in users [0 :email-address]) "kawasima1016@gmail.com"))
        (is (= (count (get-in users [0 :groups])) 1))
        (is (= (get-in users [0 :groups 0 :name]) "group1")))))

  (deftest no-conditions
    (with-entity-manager
      (let [users (em/search User)]
        (is (= (count users) 1)))))
  
  (deftest jpql
    (with-entity-manager
      (let [users (em/search User
                             :jpql "SELECT u FROM User u WHERE u.familyName = :name"
                             :params {:name "Kawashima"})]
        (is (= (count users) 1)))
      (let [users (em/search User
                             :jpql "SELECT u FROM User u WHERE u.familyName = ?1"
                             :params ["Kawashima"])]
        (is (= (count users) 1)))))

  (deftest native-sql
    (with-entity-manager
      (let [users (em/search User
                             :sql "SELECT * FROM user WHERE familyname = ?"
                             :params ["Kawashima"])]
        (is (= (count users) 1)))))

  (deftest no-result-class
    (with-entity-manager
      (let [users (-> (em/create-native-query "SELECT * FROM user")
                      (query/result-list em/*em*))]
        (is (= (count users) 1))))))


