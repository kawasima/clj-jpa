(ns clj-jpa.middleware
  (:require [clj-jpa.util :refer [with-entity-manager with-transaction]]))

(defn wrap-entity-manager [handler]
  (fn [request]
    (with-entity-manager
      (handler request))))

(defn wrap-transaction [handler]
  (fn [request]
    (with-transaction
      (handler request))))

