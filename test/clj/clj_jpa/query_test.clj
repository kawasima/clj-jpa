(ns clj-jpa.query-test
  (:require [clj-jpa.core :refer :all]
            [clj-jpa.entity-manager :as em]
            [clj-jpa.query :as query]
            [clj-jpa.fixture :refer :all]
            [clojure.test :refer :all])
  (:import [cljjpa.model User Group Membership]))


(defn query-setup [f]
  (with-entity-manager
    (with-transaction
      (doseq [id (range 100)]
        (em/merge User {:family-name (str "user" id)
                        :last-name (str "user" id)
                        :email-address (str "user" id "@example.com")
                        :age (rand-int 99)}))))
  (f))

(use-fixtures :once wrap-weld-setup)
(use-fixtures :each (join-fixtures [wrap-table-setup query-setup]))

(testing "Query API"
  (deftest limit-offset-jpql
    (with-entity-manager
      (let [users (em/search User
                             :jpql "SELECT u FROM User u ORDER BY u.id"
                             :offset 5
                             :limit 10)]
        (is (= (-> users first :family-name) "user5"))
        (is (= (-> users last :family-name) "user14")))))

  (deftest limit-only-jpql
    (with-entity-manager
      (let [users (em/search User
                             :jpql "SELECT u FROM User u ORDER BY u.id"
                             :limit 5)]
        (is (= (count users) 5))
        (is (= (-> users first :family-name) "user0"))
        (is (= (-> users last :family-name) "user4")))))

  (deftest offset-only-jpql
    (with-entity-manager
      (let [users (em/search User
                             :jpql "SELECT u FROM User u ORDER BY u.id"
                             :offset 95)]
        (is (= (count users) 5))
        (is (= (-> users last :family-name) "user99")))))

  (deftest limit-offset-criteria
    (with-entity-manager
      (let [users (em/search User
                             :offset 5
                             :limit 10
                             :order [(desc :id)])]
        (is (= (count users) 10))))))

