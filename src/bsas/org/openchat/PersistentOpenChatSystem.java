package bsas.org.openchat;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class PersistentOpenChatSystem extends OpenChatSystem {

    private Session session;

    public PersistentOpenChatSystem(Clock clock) {
        super(clock);
    }

    @Override
    public void start() {
        Configuration configuration = new Configuration();
        configuration.configure();

        ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
        SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        session = sessionFactory.openSession();
    }

    @Override
    public void beginTransaction() {
        session.beginTransaction();
    }

    @Override
    public void commitTransaction() {
        session.getTransaction().commit();
    }

    @Override
    public void stop() {
        session.close();
    }
}
