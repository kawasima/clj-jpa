(ns clj-jpa.merge-test
  (:require [clj-jpa.core :refer :all]
            [clj-jpa.entity-manager :as em]
            [clj-jpa.query :as query]
            [clj-jpa.fixture :refer :all]
            [clojure.test :refer :all])
  (:import [cljjpa.model User Group Membership]))

(use-fixtures :once wrap-weld-setup)
(use-fixtures :each wrap-table-setup)

(testing "Merge API"
  (deftest merge-normally
    (with-entity-manager
      (with-transaction
        (em/merge User {:family-name "Kawashima"
                        :last-name "Yoshitaka"
                        :email-address "kawasima1016@gmail.com"})
        (em/merge Group {:name "group1"
                         :description "gggg"})
        (let [user (first (em/search User
                                   :where (and (= :family-name "Kawashima")
                                               (= :last-name   "Yoshitaka"))))
            group (first (em/search Group
                                    :where (= :name "group1")))]
        (is (not (nil? user)))
        (is (not (nil? group)))

        (em/merge Membership {:group group
                              :user user})))))

  
  (deftest nested-entity-manager
    (with-entity-manager
      (with-transaction
        (em/merge User {:family-name "user1"
                        :last-name "user1"
                        :email-address "user1@example.com"})
        (with-entity-manager
          (with-transaction
            (em/merge User {:family-name "user2"
                            :last-name "user2"
                            :email-address "user2@example.com"})))
        (with-entity-manager
          (let [user2 (em/search User
                        :jpql "SELECT u FROM User u WHERE u.familyName = ?1"
                        :params ["user2"])
                user1 (em/search User
                        :jpql "SELECT u FROM User u WHERE u.familyName = ?1"
                        :params ["user1"])]
            (is (= (count user1) 0))
            (is (= (count user2) 1))))))))


