package bsas.org.openchat;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OpenChatSystem {
    public static final String CANNOT_REGISTER_SAME_USER_TWICE = "Username already in use.";
    public static final String USER_NOT_REGISTERED = "User not registered";
    public static final String INVALID_PUBLICATION = "Invalid post";

    private final Map<String,UserCard> userCards = new HashMap<>();
    private final Clock clock;

    public OpenChatSystem(Clock clock){
        this.clock = clock;
    }

    public boolean hasUsers() {
        return !userCards.isEmpty();
    }

    public User register(String userName, String password, String about, String homePage) {
        assertIsNotDuplicated(userName);

        final User newUser = User.named(userName, about,homePage);
        userCards.put(
                newUser.restId(),
                UserCard.of(newUser,password, Publisher.relatedTo(newUser)));

        return newUser;
    }

    public boolean hasUserNamed(String potentialUserName) {
        return userNamed(potentialUserName).isPresent();
    }

    public int numberOfUsers() {
        return userCards.size();
    }

    public <T> T withAuthenticatedUserDo(String userName, String password,
                                         Function<User,T> authenticatedClosure,
                                         Supplier<T> failedClosure) {
        return authenticatedUser(userName, password)
                .map(authenticatedClosure)
                .orElseGet(failedClosure);
    }

    public Publication publishForUserIdentifiedAs(String userId, String message) {
        final Publication newPublication = publisherForUserId(userId).publish(message, clock.now());

        return newPublication;
    }

    public List<Publication> timeLineForUserIdentifiedAs(String userId) {
        return publisherForUserId(userId).timeLine();
    }

    public void followedByFollowerIdentifiedAs(String followedUserId, String followerUserId) {
        Publisher followed = publisherForUserId(followedUserId);
        Publisher follower = publisherForUserId(followerUserId);

        followed.followedBy(follower);
    }

    public List<User> followersOfUserIdentifiedAs(String userId) {
        return publisherForUserId(userId).followers().stream()
                .map(publisher -> publisher.relatedUser())
                .collect(Collectors.toList());
    }

    public List<Publication> wallForUserIdentifiedAs(String userId) {
        return publisherForUserId(userId).wall();
    }

    public List<User> users() {
        return userCards.values().stream()
                .map(userCard->userCard.user())
                .collect(Collectors.toList());
    }

    private void assertIsNotDuplicated(String userName) {
        if(hasUserNamed(userName))
            throw new ModelException(CANNOT_REGISTER_SAME_USER_TWICE);
    }

    private Optional<User> authenticatedUser(String userName, String password) {
        return userNamed(userName)
                .filter(foundCard -> foundCard.isPassword(password))
                .map(foundCard -> foundCard.user());
    }

    public Optional<UserCard> userNamed(String potentialUserName) {
        return userCardsStream()
                .filter(userCard -> userCard.isUserNamed(potentialUserName))
                .findFirst();
    }
    private Optional<UserCard> userCardForUserId(String userId) {
        return Optional.ofNullable(userCards.get(userId));
    }

    private Publisher publisherForUserId(String userId) {
        return userCardForUserId(userId)
                .map(userCard -> userCard.publisher())
                .orElseThrow(()-> new ModelException(USER_NOT_REGISTERED));
    }

    public int likePublicationIdentifiedAs(String userId, String publicationId) {
        final Publication publication = userCardsStream()
                .flatMap(userCard -> userCard.publications())
                .filter(registeredPublication -> registeredPublication.isIdentifiedAs(publicationId))
                .findFirst()
                .orElseThrow(() -> new ModelException(INVALID_PUBLICATION));

        publication.addLiker(publisherForUserId(userId));

        return publication.likes();
    }

    private Stream<UserCard> userCardsStream() {
        return userCards.values().stream();
    }

    public void start() {
    }

    public void beginTransaction() {
    }

    public void commitTransaction() {
    }

    public void stop() {
    }
}