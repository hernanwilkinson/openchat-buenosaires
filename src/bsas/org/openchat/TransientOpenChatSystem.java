package bsas.org.openchat;

import java.util.*;
import java.util.stream.Collectors;

public class TransientOpenChatSystem extends OpenChatSystem {

    private Map<String,UserCard> userCards;

    public TransientOpenChatSystem(Clock clock){
        super(clock);
    }

    @Override
    public void start() {
        userCards = new HashMap<>();
    }

    @Override
    public void beginTransaction() {
    }

    @Override
    public void commit() {
    }

    @Override
    public void stop() {
        userCards = null;
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
        return userCards.values().stream()
                .filter(userCard -> userCard.isUserNamed(potentialUserName))
                .findFirst();
    }

    @Override
    public int numberOfUsers() {
        return userCards.size();
    }

    @Override
    public List<User> users() {
        return userCards.values().stream()
                .map(userCard->userCard.user())
                .collect(Collectors.toList());
    }

    protected Optional<UserCard> userCardIdentifiedAs(String userId) {
        return Optional.ofNullable(userCards.get(userId));
    }

    @Override
    protected Publication publicationIdentifiedAs(String publicationId) {
        return userCards.values().stream()
                .flatMap(userCard->userCard.publications())
                .filter(publication -> publication.isIdentifiedAs(publicationId))
                .findFirst()
                .orElseThrow(()->new ModelException(INVALID_PUBLICATION));
    }

}