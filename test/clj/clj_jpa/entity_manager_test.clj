(ns clj-jpa.entity-manager-test
  (:require [clj-jpa.core :refer :all]
            [clj-jpa.entity-manager :as em]
            [clojure.test :refer :all])
  (:import [org.jboss.weld.environment.se Weld]
           [cljjpa.model User Group]))

(.. (Weld.) initialize)

(deftest entity-manager-creation
  (with-entity-manager
    (with-transaction
      (let [user (em/merge User {:family-name "Kawashima"
                                 :last-name "Yoshitaka"
                                 :email-address "kawasima1016@gmail.com"})]
        (em/merge Group {:name "group1"
                         :users [user]}))))
  
  (with-entity-manager
    (let [users (em/search User
                           :where (and (= :family-name "Kawashima")
                                       (= :last-name   "Yoshitaka")))]
      (is (= (count users) 1))
      (is (= (get-in users [0 :email-address]) "kawasima1016@gmail.com"))
      (is (= (count (get-in users [0 :groups])) 1))
      (is (= (get-in users [0 :groups 0 :name]) "group1")))))

