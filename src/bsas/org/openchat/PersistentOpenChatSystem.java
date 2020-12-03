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

    private Session session;
    private static SessionFactory sessionFactory;

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
    public void stop() {
        session.close();
    }

    @Override
    public void rollbackTransaction() {
        session.getTransaction().rollback();
    }

    @Override
    public void reset() {
        sessionFactory = null;
    }

    @Override
    public User register(String userName, String password, String about, String homePage) {
        assertIsNotDuplicated(userName);

        final User newUser = User.named(userName, about,homePage);
        final Publisher newPublisher = Publisher.relatedTo(newUser);
        final UserCard newUserCard = UserCard.of(newUser, password, newPublisher);
        session.persist(newUser);
        session.persist(newPublisher);
        session.persist(newUserCard);

        return newUser;
    }

    @Override
    public boolean hasUsers() {
        return numberOfUsers()>0;
    }

    @Override
    public Optional<UserCard> userNamed(String potentialUserName) {
        final UserCard found = (UserCard) session
                .createQuery("SELECT a FROM UserCard a JOIN a.user b WHERE b.name = '" +potentialUserName+"'")
                .uniqueResult();

        return Optional.ofNullable(found);
    }

    @Override
    public int numberOfUsers() {
        return session.createCriteria(UserCard.class).list().size();
    }

    @Override
    protected Optional<UserCard> userCardForUserId(String userId) {
        final UserCard found = (UserCard) session
                .createQuery("SELECT a FROM UserCard a JOIN a.user b WHERE b.restId = '" +userId+"'")
                .uniqueResult();

        return Optional.ofNullable(found);
    }

    @Override
    public Publication publicationIdentifiedAs(String publicationId) {
        final Publication found = (Publication) session
                .createCriteria(Publication.class)
                .add(Restrictions.eq("restId", publicationId))
                .uniqueResult();

        if(found==null) throw new ModelException(INVALID_PUBLICATION);
        return found;
    }

    @Override
    protected Stream<UserCard> userCardsStream() {
        return session
                .createCriteria(UserCard.class)
                .list()
                .stream();
    }
}
