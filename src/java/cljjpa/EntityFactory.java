package cljjpa;

import clojure.lang.*;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ListAttribute;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
        E entity;
        EntityType<E> entityType = em.getMetamodel().entity(entityClass);

        if (map instanceof JPAEntityMap && ((JPAEntityMap) map).meta().valAt(Keyword.intern("entity")) != null) {
            entity = (E)((JPAEntityMap) map).meta().valAt(Keyword.intern("entity"));
        } else {
            entity = entityClass.newInstance();
        }

        for (final Attribute<E, ?> attribute : entityType.getDeclaredAttributes()) {
            Keyword mapKey = Keyword.intern(CaseConversionUtils.toKebab(attribute.getName()));
            if (map.containsKey(mapKey)) {
                Object value = map.valAt(mapKey);

                if (value instanceof IPersistentMap) {
                    if (attribute.isAssociation() && em.getMetamodel().getEntities().contains(attribute.getDeclaringType())) {
                        setAttributeValue(entity, attribute, new EntityFactory(em, attribute.getJavaType()).create((IPersistentMap) value));
                    }
                } else if (value instanceof Seqable) {
                    if (attribute.isCollection() && List.class.isAssignableFrom(attribute.getJavaType())) {
                        SeqIterator iter = new SeqIterator(((Seqable) value).seq());
                        List<Object> listAttribute = (List<Object>) getAttributeValue(attribute, entity);
                        if (listAttribute == null) {
                            listAttribute = new ArrayList<>();
                            setAttributeValue(entity, attribute, listAttribute);
                        }
                        listAttribute.clear();
                        while(iter.hasNext()) {
                            Object item = iter.next();
                            if (item instanceof IPersistentMap && attribute.isAssociation()) {
                                listAttribute.add(
                                        new EntityFactory(em, ((ListAttribute<E, ?>) attribute).getElementType().getJavaType())
                                                .create((IPersistentMap) item));
                            } else {
                                listAttribute.add(item);
                            }
                        }
                    }
                } else {
                    setAttributeValue(entity, attribute, value);
                }
            }
        }

        return entity;
    }

    private Object getAttributeValue(Attribute<E, ?> attribute, E entity) {
        Member member = attribute.getJavaMember();
        try {
            if (member instanceof Method) {
                return ((Method) member).invoke(entity);
            } else if (member instanceof Field) {
                return ((Field) member).get(entity);
            } else {
                throw new IllegalStateException("The attribute of java member is unknown.");
            }
        } catch (IllegalAccessException| InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
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
