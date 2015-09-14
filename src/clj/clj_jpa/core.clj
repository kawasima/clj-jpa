(ns clj-jpa.core
  (:require [clj-jpa.util]))

(defmacro with-entity-manager [& body]
  `(clj-jpa.util/with-entity-manager ~@body))

(defmacro with-transaction [& body]
  `(clj-jpa.util/with-transaction ~@body))

