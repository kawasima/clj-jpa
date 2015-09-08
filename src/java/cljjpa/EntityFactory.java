package cljjpa;

import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author kawasima
 */
public class EntityFactory<E> {
    final EntityManager em;
    final Class<E> entityClass;

    public EntityFactory(EntityManager em, Class<E> entityClass) {
        this.em = em;
        this.entityClass = entityClass;
    }

    public E create(IPersistentMap map) throws Exception {
        EntityType<E> entityType = em.getMetamodel().entity(entityClass);
        E entity = entityClass.newInstance();
        for (final Attribute<E, ?> attribute : entityType.getDeclaredAttributes()) {
            Object value = map.valAt(Keyword.intern(CaseConversionUtils.toKebab(attribute.getName())));
            if (value instanceof IPersistentMap) {
                if (attribute.isAssociation() && em.getMetamodel().getEntities().contains(attribute.getJavaType())) {
                    setAttributeValue(entity, attribute, new EntityFactory(em, attribute.getJavaType()).create((IPersistentMap) value));
                }
            } else if (value instanceof ISeq) {
                if (attribute.isCollection() && List.class.isAssignableFrom(attribute.getJavaType())) {
                    throw new UnsupportedOperationException("Inner list");
                    /*
                    List<?> list = new ArrayList<>();
                    ISeq seq = (ISeq) value;
                    while(seq.count() > 0) {
                        seq.first();
                        seq = seq.next();
                        if (seq == null) {
                            break;
                        }
                    }
                    */
                }
            } else {
                setAttributeValue(entity, attribute, value);
            }
        }
        return entity;

    }

    private void setAttributeValue(E entity, Attribute<E, ?> attribute, Object value) {
        Member member = attribute.getJavaMember();
        if (member instanceof Method) {
            try {
                ((Method) member).invoke(entity, value);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        } else if (member instanceof Field) {
            try {
                ((Field) member).set(entity, value);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        } else {
            throw new IllegalStateException("Unknown member type.");
        }

    }
}
