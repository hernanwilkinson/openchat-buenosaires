package bsas.org.openchat;

import java.util.*;
import java.util.stream.Stream;

public class TransientOpenChatSystem extends OpenChatSystem {

    private Map<String,UserCard> userCards = new HashMap<>();

    public TransientOpenChatSystem(Clock clock){
        super(clock);
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
    public void rollbackTransaction() {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean hasUsers() {
        return !userCards.isEmpty();
    }

    @Override
    public User register(String userName, String password, String about, String homePage) {
        assertIsNotDuplicated(userName);

        final User newUser = User.named(userName, about,homePage);
        userCards.put(
                newUser.id(),
                UserCard.of(newUser,password, Publisher.relatedTo(newUser)));

        return newUser;
    }

    @Override
    public boolean hasUserNamed(String potentialUserName) {
        return userNamed(potentialUserName).isPresent();
    }

    @Override
    public Optional<UserCard> userNamed(String potentialUserName) {
        return userCardsStream()
                .filter(userCard -> userCard.isUserNamed(potentialUserName))
                .findFirst();
    }

    @Override
    public int numberOfUsers() {
        return userCards.size();
    }

    @Override
    public Stream<UserCard> userCardsStream() {
        return userCards.values().stream();
    }

    protected Optional<UserCard> userCardIdentifiedAs(String userId) {
        return Optional.ofNullable(userCards.get(userId));
    }

    @Override
    protected Publication publicationIdentifiedAs(String publicationId) {
        return userCardsStream()
                .flatMap(userCard->userCard.publications())
                .filter(publication -> publication.isIdentifiedAs(publicationId))
                .findFirst()
                .orElseThrow(()->new ModelException(INVALID_PUBLICATION));
    }

    @Override
    public void reset() {
    }

}