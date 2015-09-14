(ns clj-jpa.merge-test
  (:require [clj-jpa.core :refer :all]
            [clj-jpa.entity-manager :as em]
            [clj-jpa.query :as query]
            [clojure.test :refer :all])
  (:import [org.jboss.weld.environment.se Weld]
           [cljjpa.model User Group Membership]))

(.. (Weld.) initialize)

(testing "Merge API"
  (with-entity-manager
    (with-transaction
      (-> (em/create-native-query "DELETE FROM membership")
          (query/execute-update)) 
      (-> (em/create-native-query "DELETE FROM user")
          (query/execute-update)) 
      (-> (em/create-native-query "DELETE FROM groups")
          (query/execute-update))
      (let [user (em/merge User {:family-name "Kawashima"
                                 :last-name "Yoshitaka"
                                 :email-address "kawasima1016@gmail.com"})]
        (em/merge Group {:name "group1"
                         :description "gggg"}))))

  (deftest merge-normally
    (with-entity-manager
      (with-transaction
        (let [user (first (em/search User
                                   :where (and (= :family-name "Kawashima")
                                               (= :last-name   "Yoshitaka"))))
            group (first (em/search Group
                                    :where (= :name "group1")))]
        (is (not (nil? user)))
        (is (not (nil? group)))

        (em/merge Membership {:group group
                                :user user}))))))


