package bsas.org.openchat;

import java.util.*;
import java.util.stream.Stream;

public class TransientOpenChatSystem extends OpenChatSystem {

    protected final Map<String,UserCard> userCards = new HashMap<>();

    public TransientOpenChatSystem(Clock clock){
        super(clock);
    }

    public boolean hasUsers() {
        return !userCards.isEmpty();
    }

    public long numberOfUsers() {
        return userCards.size();
    }

    @Override
    public User register(String userName, String password, String about, String homePage) {
        assertIsNotDuplicated(userName);

        final User newUser = User.named(userName, about,homePage);
        userCards.put(
                newUser.restId(),
                UserCard.of(newUser,password, Publisher.relatedTo(newUser)));

        return newUser;
    }

    @Override
    public Optional<UserCard> userNamed(String potentialUserName) {
        return userCardsStream()
                .filter(userCard -> userCard.isUserNamed(potentialUserName))
                .findFirst();
    }
    @Override
    protected Optional<UserCard> userCardForUserId(String userId) {
        return Optional.ofNullable(userCards.get(userId));
    }

    @Override
    public Publication publicationIdentifiedAs(String publicationId) {
        return userCardsStream()
                .flatMap(userCard -> userCard.publications())
                .filter(registeredPublication -> registeredPublication.isIdentifiedAs(publicationId))
                .findFirst()
                .orElseThrow(() -> new ModelException(INVALID_PUBLICATION));
    }

    @Override
    protected Stream<UserCard> userCardsStream() {
        return userCards.values().stream();
    }

    @Override
    public void start() {
    }

    @Override
    public void beginTransaction() {
    }

    @Override
    public void commitTransaction() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void rollbackTransaction() {
    }

    @Override
    public void reset() {
    }

    @Override
    protected Publisher publisherForUserId(String userId) {
        return userCardForUserId(userId)
                .map(userCard -> userCard.publisher())
                .orElseThrow(()-> new ModelException(USER_NOT_REGISTERED));
    }
}