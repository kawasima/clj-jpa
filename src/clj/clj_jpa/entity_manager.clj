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
  ([m]
   (if-let [entity (:entity (meta m))]
     (JPAEntityMap. *em*
                    (.. *em* (merge (.. (EntityFactory. *em* (class entity)) (create m)))))))
  ([entity-class m]
   (let [entity (.. (EntityFactory. *em* entity-class) (create m))]
     (JPAEntityMap. *em* (.. *em* (merge entity))))))

(defn remove
  ([m]
   (if-let [entity (:entity (meta m))]
     (.. *em* (remove (.. (EntityFactory. *em* (class entity)) (create m))))))
  ([entity-class m]
   (let [entity (.. (EntityFactory. *em* entity-class) (create m))]
     (.. *em* (remove entity)))))

(defn find [entity-class id]
  (when-let [entity (.find *em* entity-class id)]
    (JPAEntityMap. *em* entity)))

(defn lock [m lock-mode]
  (if-let [entity (:entity (meta m))]
    (.. *em* (lock (.. (EntityFactory. *em* (class entity)) (create m)) lock-mode))))

(defn create-named-query
  ([query-name entity-class]
   (.. *em* (createNamedQuery query-name entity-class))))

(defn create-native-query
  ([query-name]
   (.. *em* (createNativeQuery query-name)))
  ([query-name entity-class]
   (.. *em* (createNativeQuery query-name entity-class))))

(defn search-by-criteria [entity-class {:keys [where order limit offset] :as options}]
  (let [builder (gensym 'builder)
        root (gensym 'root)
        criteria-query (gensym 'citeria-query)
        query (gensym 'query)]
    `(let [~builder (.getCriteriaBuilder ~'clj-jpa.entity-manager/*em*)
           ~criteria-query (.createQuery ~builder ~entity-class)
           ~root (.from ~criteria-query ~entity-class)]
       ~(when-let [where (:where `~options)]
          `(.where ~criteria-query ~(query/parse-where builder root `~where)))
       
       ~(when `~order
         `(.orderBy ~criteria-query ~(query/parse-order builder root `~order)))
       (let [~query (.. ~'clj-jpa.entity-manager/*em*
                        (createQuery ~criteria-query))]
         (when ~limit
           (query/limit ~query ~limit))
         (when ~offset
           (query/offset ~query ~offset))
         (query/result-list ~query ~'clj-jpa.entity-manager/*em*)))))

(defn search-by-sql [entity-class {:keys [sql params]}]
  (let [query (.. *em* (createNativeQuery sql entity-class))]
     (when params
       (query/parameters query params))
     (query/result-list query *em*)))

(defn search-by-jpql [entity-class {:keys [jpql params limit offset]}]
  (let [query (.. *em* (createQuery jpql entity-class))]
     (when params
       (query/parameters query params))
     (when limit
       (query/limit query limit))
     (when offset
       (query/offset query offset))
     (query/result-list query *em*)))

(defmacro search [entity-class & {:as options}]
  (cond
    (:sql options)  `(search-by-sql ~entity-class ~options)
    (:jpql options) `(search-by-jpql ~entity-class ~options)
    :else (search-by-criteria entity-class options)))
