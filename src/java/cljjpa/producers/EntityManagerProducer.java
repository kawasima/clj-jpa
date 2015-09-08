package cljjpa.producers;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * @author kawasima
 */
@Dependent
public class EntityManagerProducer {
    @Inject
    EntityManagerFactory factory;

    @Produces
    public EntityManager getEntityManager() {
        return factory.createEntityManager();
    }

}
