package com.parking.model.business.impl;

/**
 * Created by Yurii on 28.01.2017.
 */



import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Created by Yurii on 26.01.2017.
 */
public class HibernateJPAUtil {

    private static final String PERSISTENCE_UNIT_NAME = "psunit1";
    private static EntityManagerFactory factory=null;

    static {
        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return factory;
    }
}

