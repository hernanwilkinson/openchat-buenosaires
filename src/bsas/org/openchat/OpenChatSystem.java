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
                newUser.id(),
                UserCard.of(newUser,password, Publisher.relatedTo(newUser)));

        return newUser;
    }

    public boolean hasUserNamed(String potentialUserName) {
        return userNamed(potentialUserName).isPresent();
    }

    public Optional<UserCard> userNamed(String potentialUserName) {
        return userCards.values().stream()
                .filter(userCard -> userCard.isUserNamed(potentialUserName))
                .findFirst();
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
        final Publication newPublication = publisherIdentifiedAs(userId).publish(message, clock.now());

        return newPublication;
    }

    public List<Publication> timeLineOfUserIdentifiedAs(String userId) {
        return publisherIdentifiedAs(userId).timeLine();
    }

    public void followedByFollowerIdentifiedAs(String followedUserId, String followerUserId) {
        Publisher followed = publisherIdentifiedAs(followedUserId);
        Publisher follower = publisherIdentifiedAs(followerUserId);

        followed.followedBy(follower);
    }

    public List<User> followersOfUserIdentifiedAs(String userId) {
        return publisherIdentifiedAs(userId).followers().stream()
                .map(publisher -> publisher.relatedUser())
                .collect(Collectors.toList());
    }

    public List<Publication> wallOfUserIdentifiedAs(String userId) {
        return publisherIdentifiedAs(userId).wall();
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

    private Optional<UserCard> userCardIdentifiedAs(String userId) {
        return Optional.ofNullable(userCards.get(userId));
    }

    private Publisher publisherIdentifiedAs(String userId) {
        return userCardIdentifiedAs(userId)
                .map(userCard -> userCard.publisher())
                .orElseThrow(()-> new ModelException(USER_NOT_REGISTERED));
    }

    public int likesOf(Publication publication) {
        return publication.likes();
    }

    public int likePublication(Publication externalPublication, String userId) {
        final Publication publication = publicationIdentifiedAs(externalPublication.id());

        publication.addLiker(publisherIdentifiedAs(userId));

        return publication.likes();
    }

    public int likePublicationIdentifiedAs(String publicationId, String userName) {
        return likePublication(publicationIdentifiedAs(publicationId),userName);
    }

    private Publication publicationIdentifiedAs(String publicationId) {
        return userCards.values().stream()
                .flatMap(userCard->userCard.publications())
                .filter(publication -> publication.isIdentifiedAs(publicationId))
                .findFirst()
                .orElseThrow(()->new ModelException(INVALID_PUBLICATION));
    }

    private static class UserCard {

        private final User user;
        private final String password;
        private final Publisher publisher;

        public UserCard(User user, String password, Publisher publisher) {
            this.user = user;
            this.password = password;
            this.publisher = publisher;
        }

        public static UserCard of(User user, String password, Publisher publisher) {
            return new UserCard(user,password,publisher);
        }

        public User user() {
            return user;
        }

        public boolean isPassword(String potentialPassword) {
            return password.equals(potentialPassword);
        }

        public Publisher publisher() {
            return publisher;
        }

        public Stream<Publication> publications() {
            return publisher.publications();
        }

        public boolean isUserNamed(String potentialUserName) {
            return user.isNamed(potentialUserName);
        }
    }
}