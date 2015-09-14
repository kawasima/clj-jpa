(ns clj-jpa.fixture
  (:require [clj-jpa.core :refer :all]
            [clj-jpa.entity-manager :as em]
            [clj-jpa.query :as query])
  (:import [org.jboss.weld.environment.se Weld]
           [cljjpa.model User Group]))

(defn wrap-weld-setup [f]
  (.. (Weld.) initialize)
  (f))

(defn wrap-table-setup [f]
  (with-entity-manager
    (with-transaction
      (-> (em/create-native-query "DELETE FROM membership")
          (query/execute-update)) 
      (-> (em/create-native-query "DELETE FROM user")
          (query/execute-update)) 
      (-> (em/create-native-query "DELETE FROM groups")
          (query/execute-update))))
  (f))

(defn wrap-table-setup-with-data [f]
  (with-entity-manager
    (with-transaction
      (-> (em/create-native-query "DELETE FROM membership")
          (query/execute-update)) 
      (-> (em/create-native-query "DELETE FROM user")
          (query/execute-update)) 
      (-> (em/create-native-query "DELETE FROM groups")
          (query/execute-update))
      ))
  (f))

