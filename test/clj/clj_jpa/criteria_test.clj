(ns clj-jpa.criteria-test
  (:require [clj-jpa.core :refer :all]
            [clj-jpa.entity-manager :as em]
            [clj-jpa.query :as query]
            [clj-jpa.fixture :refer :all]
            [clojure.test :refer :all])
  (:import  [cljjpa.model User Group Membership]))

(defn criteria-setup [f]
  (with-entity-manager
    (with-transaction
      (em/merge User {:family-name "user1"
                      :last-name "user1"
                      :email-address "user1@example.com"
                      :age (int 10)})
      (em/merge User {:family-name "user2"
                      :last-name "user2"
                      :email-address "user2@example.com"
                      :age (int 20)})
      (em/merge User {:family-name "user3"
                      :last-name "user3"
                      :email-address "user3@example.com"
                      :age (int 30)})
      (em/merge User {:family-name "user4"
                      :last-name "user4"
                      :email-address "user4@gmail.com"
                      :age (int 40)})))
  (f))

(use-fixtures :once wrap-weld-setup)
(use-fixtures :each (join-fixtures [wrap-table-setup criteria-setup]))

(testing "Criteria API"
  (deftest criteria-equal
    (with-entity-manager
      (let [users (em/search User
                             :where (= :family-name "user1"))]
        (is (= (count users) 1))
        (is (= (-> users first :family-name) "user1")))))

    (deftest criteria-not-equal
      (with-entity-manager
        (let [users (em/search User
                               :where (not= :family-name "user1"))]
          (is (= (count users) 3)))))

    (deftest criteria-like
      (with-entity-manager
        (let [users (em/search User
                               :where (like :family-name "user%"))]
          (is (= (count users) 4)))
        
        (let [users (em/search User
                               :where (like :family-name "%er4%"))]
          (is (= (count users) 1)))))

    (deftest criteria->
      (with-entity-manager
        (let [users (em/search User :where (> :age 10))]
          (is (= (count users) 3)))
        (let [users (em/search User :where (> :age 20))]
          (is (= (count users) 2)))
        (let [users (em/search User :where (> :age 30))]
          (is (= (count users) 1)))))

    (deftest criteria-<
      (with-entity-manager
        (let [users (em/search User :where (< :age 10))]
          (is (= (count users) 0)))
        (let [users (em/search User :where (< :age 20))]
          (is (= (count users) 1)))
        (let [users (em/search User :where (< :age 30))]
          (is (= (count users) 2)))))

    (deftest criteria->=
      (with-entity-manager
        (let [users (em/search User :where (>= :age 10))]
          (is (= (count users) 4)))
        (let [users (em/search User :where (>= :age 20))]
          (is (= (count users) 3)))
        (let [users (em/search User :where (>= :age 30))]
          (is (= (count users) 2)))))

    (deftest criteria-<=
      (with-entity-manager
        (let [users (em/search User :where (<= :age 10))]
          (is (= (count users) 1)))
        (let [users (em/search User :where (<= :age 20))]
          (is (= (count users) 2)))
        (let [users (em/search User :where (<= :age 30))]
          (is (= (count users) 3)))))

    (deftest criteria-in
      (with-entity-manager
        (let [users (em/search User :where (in :family-name ["user1" "user2"]))]
          (is (= (count users) 2))
          (is (= #{"user1" "user2"}  (apply hash-set (map :family-name users)))))))

    (deftest criteria-or
      (with-entity-manager
        (let [users (em/search User :where (or (= :family-name "user1")
                                               (= :family-name "user2")))]
          (is (= (count users) 2))
          (is (= #{"user1" "user2"}  (apply hash-set (map :family-name users))))))))
