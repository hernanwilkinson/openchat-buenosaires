package bsas.org.openchat;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import java.util.Optional;
import java.util.stream.Stream;

public class PersistentOpenChatSystem extends OpenChatSystem {
    private static SessionFactory sessionFactory;
    private Session session;

    public PersistentOpenChatSystem(Clock clock) {
        super(clock);
    }

    @Override
    public void start() {
        session = sessionFactory().openSession();
    }

    public static synchronized SessionFactory sessionFactory() {
        if(sessionFactory==null) {
            Configuration configuration = new Configuration();
            configuration.configure();

            ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        }
        return sessionFactory;
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
    public void rollbackTransaction() {
        session.getTransaction().rollback();
    }

    @Override
    public void stop() {
        session.close();
    }

    @Override
    public boolean hasUsers() {
        return numberOfUsers()>0;

    }

    @Override
    public User register(String userName, String password, String about, String homePage) {
        System.out.println("registrando");
        assertIsNotDuplicated(userName);
        //sleep();
        final User newUser = User.named(userName, about,homePage);
        final Publisher newPublisher = Publisher.relatedTo(newUser);
        final UserCard newUserCard = UserCard.of(newUser, password, newPublisher);
        session.persist(newPublisher);
        session.persist(newUserCard);

        return newUser;
    }

    public void sleep() {
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public boolean hasUserNamed(String potentialUserName) {
        return userNamed(potentialUserName).isPresent();
    }

    @Override
    public Optional<UserCard> userNamed(String potentialUserName) {
        final UserCard found = (UserCard) session
                .createCriteria(UserCard.class, "userCard")
                .add(Restrictions.eq("userCard.user.name", potentialUserName))
                .uniqueResult();
        return Optional.ofNullable (found);
    }

    @Override
    public int numberOfUsers() {
        return session.createCriteria(UserCard.class).list().size();
    }

    @Override
    public Stream<UserCard> userCardsStream() {
        return (Stream<UserCard>) session
                .createCriteria(UserCard.class)
                .list()
                .stream();
    }

    @Override
    protected Optional<UserCard> userCardIdentifiedAs(String userId) {
        final UserCard found = (UserCard) session
                .createCriteria(UserCard.class,"userCard")
                .add(Restrictions.eq("userCard.user.id", userId))
                .uniqueResult();
        return Optional.ofNullable (found);
    }

    @Override
    protected Publication publicationIdentifiedAs(String publicationId) {
        final Publication found = (Publication) session
                .createCriteria(Publication.class)
                .add(Restrictions.eq("id", publicationId))
                .uniqueResult();
        if(found==null)
            throw new ModelException(INVALID_PUBLICATION);
        return found;

    }

    @Override
    public void reset() {
        sessionFactory = null;
    }
}
