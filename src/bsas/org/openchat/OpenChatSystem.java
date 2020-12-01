package bsas.org.openchat;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class OpenChatSystem {
    public static final String CANNOT_REGISTER_SAME_USER_TWICE = "Username already in use.";
    public static final String USER_NOT_REGISTERED = "User not registered";
    public static final String INVALID_PUBLICATION = "Invalid post";
    protected final Clock clock;

    public OpenChatSystem(Clock clock) {
        this.clock = clock;
    }

    public abstract void start();

    public abstract void beginTransaction();

    public abstract void commitTransaction();

    public abstract void rollbackTransaction();

    public abstract void stop();

    public abstract boolean hasUsers();

    public abstract User register(String userName, String password, String about, String homePage);

    public abstract boolean hasUserNamed(String potentialUserName);

    public abstract Optional<UserCard> userNamed(String potentialUserName);

    public abstract int numberOfUsers();

    public <T> T withAuthenticatedUserDo(String userName, String password,
                                         Function<User, T> authenticatedClosure,
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
        return userCardsStream()
                .map(userCard->userCard.user())
                .collect(Collectors.toList());
    }

    public void assertIsNotDuplicated(String userName) {
        if(hasUserNamed(userName))
            throw new ModelException(CANNOT_REGISTER_SAME_USER_TWICE);
    }

    Optional<User> authenticatedUser(String userName, String password) {
        return userNamed(userName)
                .filter(foundCard -> foundCard.isPassword(password))
                .map(foundCard -> foundCard.user());
    }

    protected Publisher publisherIdentifiedAs(String userId) {
        return userCardIdentifiedAs(userId)
                .map(userCard -> userCard.publisher())
                .orElseThrow(()-> new ModelException(USER_NOT_REGISTERED));
    }

    public abstract Stream<UserCard> userCardsStream();

    protected abstract Optional<UserCard> userCardIdentifiedAs(String userId);

    public int likePublicationIdentifiedAs(String publicationId, String userName) {
        final Publication publication = publicationIdentifiedAs(publicationIdentifiedAs(publicationId).id());
        publication.addLiker(publisherIdentifiedAs(userName));

        return publication.likes();
    }

    protected abstract Publication publicationIdentifiedAs(String publicationId);

    public abstract void reset();
}
