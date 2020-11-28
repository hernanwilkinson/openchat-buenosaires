package bsas.org.openchat;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import java.util.List;
import java.util.Optional;

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
    public void commit() {
        session.getTransaction().commit();
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
        assertIsNotDuplicated(userName);

        final User newUser = User.named(userName, about,homePage);
        final Publisher newPublisher = Publisher.relatedTo(newUser);
        final UserCard newUserCard = UserCard.of(newUser, password, newPublisher);
        session.persist(newPublisher);
        session.persist(newUserCard);

        return newUser;
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
    public List<User> users() {
        throw new UnsupportedOperationException();
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
}
