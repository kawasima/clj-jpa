package cljjpa.producers;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @author kawasima
 */
@Singleton
public class EntityManagerFactoryProcducer {
    private EntityManagerFactory factory;

    @Produces
    public EntityManagerFactory getEntityManagerFactory() {
        if (factory == null) {
            factory = Persistence.createEntityManagerFactory("default");
        }
        return factory;
    }
}
