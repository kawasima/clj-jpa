package cljjpa;

import clojure.lang.*;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author kawasima
 */
public class JPAEntityMap<E> implements IPersistentMap {
    final IPersistentMap attributeMap;
    final IPersistentMap _meta;

    public JPAEntityMap(final EntityManager em, final E entity) {
        Map<Keyword, Object> attributes = new HashMap<>();
        EntityType<E> entityType = (EntityType<E>) em.getMetamodel().entity(entity.getClass());
        for (final Attribute<E, ?> attribute : entityType.getDeclaredAttributes()) {
            Object value;

            if (attribute.isAssociation()) {
                IFn fn = new AFunction() {
                    @Override
                    public Object invoke() {
                        return getAttributeValue(em, attribute, entity);
                    }
                };
                value = new Delay(fn);
            } else {
                value = getAttributeValue(em, attribute, entity);
            }
            attributes.put(Keyword.intern(CaseConversionUtils.toKebab(attribute.getName())), value);
        }
        this.attributeMap = PersistentHashMap.create(attributes);
        this._meta = PersistentHashMap.create(Keyword.intern("entity"), entity.getClass());
    }

    protected JPAEntityMap(IPersistentMap meta, IPersistentMap pmap) {
        this._meta = meta;
        this.attributeMap = pmap;
    }

    @Override
    public boolean containsKey(Object key) {
        return attributeMap.containsKey(key);
    }

    @Override
    public IMapEntry entryAt(Object key) {
        return attributeMap.entryAt(key);
    }

    @Override
    public IPersistentMap assoc(Object key, Object value) {
        return new JPAEntityMap(meta(), assoc(key, value));
    }

    @Override
    public IPersistentMap assocEx(Object key, Object value) {
        return new JPAEntityMap(meta(), assocEx(key, value));
    }

    @Override
    public IPersistentMap without(Object key) {
        return new JPAEntityMap(meta(), without(key));
    }

    @Override
    public Object valAt(Object key) {
        return Delay.force(attributeMap.valAt(key));
    }

    @Override
    public Object valAt(Object key, Object notFound) {
        Object val = valAt(key);
        if (val == null) {
            return notFound;
        } else {
            return val;
        }
    }

    @Override
    public int count() {
        return attributeMap.count();
    }

    @Override
    public IPersistentCollection cons(Object o) {
        return attributeMap.cons(o);
    }

    @Override
    public IPersistentCollection empty() {
        return attributeMap.empty();
    }

    @Override
    public boolean equiv(Object o) {
        return attributeMap.equiv(o);
    }

    @Override
    public Iterator iterator() {
        return attributeMap.iterator();
    }

    @Override
    public ISeq seq() {
        return attributeMap.seq();
    }

    public IPersistentMap meta() {
        return this._meta;
    }

    @Override
    public String toString() {
        return attributeMap.toString();
    }

    private Object wrapEntity(final EntityManager em, final Object value) {
        if (value == null) {
            return null;
        } else if (value.getClass().getAnnotation(Entity.class) != null) {
            return new JPAEntityMap(em, value);
        } else if (value instanceof List) {
            List<Object> entityList = new ArrayList<>();
            for (Object entity : (List) value) {
                entityList.add(wrapEntity(em, entity));
            }
            return PersistentVector.create(entityList);
        } else {
            return value;
        }
    }

    private Object getAttributeValue(final EntityManager em, Attribute<E, ?> attribute, E entity) {
        Member member = attribute.getJavaMember();
        try {
            if (member instanceof Method) {
                return wrapEntity(em, ((Method) member).invoke(entity));
            } else if (member instanceof Field) {
                return wrapEntity(em, ((Field) member).get(entity));
            } else {
                throw new IllegalStateException("The attribute of java member is unknown.");
            }
        } catch (IllegalAccessException| InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
}
