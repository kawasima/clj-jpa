(ns clj-jpa.entity-manager
  (:require [clojure.walk :as walk]
            [clj-jpa.query :as query])
  (:import [javax.persistence EntityManager LockModeType]
           [javax.persistence.criteria Predicate]
           [javax.enterprise.inject.spi CDI]
           [cljjpa JPAEntityMap EntityFactory])
  (:refer-clojure :exclude [find remove count merge]))

(def ^:dynamic *em*)

(defn create-entity-manager []
  (.. (CDI/current)
      (select EntityManager (make-array java.lang.annotation.Annotation 0))
      get))

(defn merge
  ([entity-class m]
   (let [entity (.. (EntityFactory. *em* entity-class) (create m))]
     (.. *em* (merge entity)))))

(defn remove
  ([m]
   (remove (-> (meta m) :entity)))
  ([entity-class m]
   (let [entity (.. (EntityFactory. *em* entity-class) (create m))]
     (.. *em* (remove entity)))))

(defn create-named-query
  ([query-name entity-class]
   (.. *em* (createNamedQuery query-name entity-class))))

(defn create-native-query [query-name entity-class]
  (.. *em* (createNativeQuery query-name entity-class)))

(defn search-by-criteria [entity-class options]
  (let [builder (gensym 'builder)
        root (gensym 'root)]
    `(let [~builder (.getCriteriaBuilder ~'clj-jpa.entity-manager/*em*)
           criteria-query# (.createQuery ~builder ~entity-class)
           ~root (.from criteria-query# ~entity-class)]
       (.where criteria-query# ~(query/parse-where builder root (:where `~options)))
       
       (let [query# (.. ~'clj-jpa.entity-manager/*em*
                        (createQuery criteria-query#))]
         (query/result-list query# ~'clj-jpa.entity-manager/*em*)))))

(defmacro search [entity-class & {:as options}]
  (cond
    :else (search-by-criteria entity-class options)))
