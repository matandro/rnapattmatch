package bgu.bioinf.rnaSequenceSniffer.db;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Created by matan on 26/11/14.
 */
public class DBConnector {
    private static EntityManagerFactory entityManagerFactory = null;

    public static EntityManager getEntityManager() {
        EntityManager em = null;
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            if (entityManagerFactory != null)
                System.err.println("EMF closed, restarting it");
            entityManagerFactory = Persistence.createEntityManagerFactory("RNASequenceSniffer");
        }
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            em = entityManagerFactory.createEntityManager();
        }
        return em;
    }

    public static void closeEMF() {
        if (entityManagerFactory != null) {
            try {
                entityManagerFactory.close();
            } catch (Exception ignore) {
            }
            entityManagerFactory = null;
        }
    }
}
