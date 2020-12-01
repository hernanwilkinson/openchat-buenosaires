package bsas.org.openchat;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

    @Override
    public User register(String userName, String password, String about, String homePage) {
        assertIsNotDuplicated(userName);

        final User newUser = User.named(userName, about,homePage);
        final Publisher publisher = Publisher.relatedTo(newUser);
        final UserCard userCard = UserCard.of(newUser, password, publisher);
        session.persist(publisher);
        session.persist(userCard);

        return newUser;
    }

    @Override
    public boolean hasUsers() {
        return numberOfUsers()>0;
    }

    @Override
    public Optional<UserCard> userNamed(String potentialUserName) {
        final UserCard found = (UserCard) session
                .createCriteria(UserCard.class, "userCard")
                .add(Restrictions.eq("userCard.user.name", potentialUserName))
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
                .createCriteria(UserCard.class, "userCard")
                .add(Restrictions.eq("userCard.user.restId", userId))
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
