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

    public abstract boolean hasUsers();

    public abstract User register(String userName, String password, String about, String homePage);

    public boolean hasUserNamed(String potentialUserName) {
        return userNamed(potentialUserName).isPresent();
    }

    public abstract int numberOfUsers();

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
        return userCardsStream()
                .map(userCard->userCard.user())
                .collect(Collectors.toList());
    }

    void assertIsNotDuplicated(String userName) {
        if(hasUserNamed(userName))
            throw new ModelException(CANNOT_REGISTER_SAME_USER_TWICE);
    }

    protected Optional<User> authenticatedUser(String userName, String password) {
        return userNamed(userName)
                .filter(foundCard -> foundCard.isPassword(password))
                .map(foundCard -> foundCard.user());
    }

    public abstract Optional<UserCard> userNamed(String potentialUserName);

    protected abstract Optional<UserCard> userCardForUserId(String userId);

    private Publisher publisherForUserId(String userId) {
        return userCardForUserId(userId)
                .map(userCard -> userCard.publisher())
                .orElseThrow(()-> new ModelException(USER_NOT_REGISTERED));
    }

    public int likePublicationIdentifiedAs(String publicationId, String userId) {
        final Publication publication = publicationIdentifiedAs(publicationId);

        publication.addLiker(publisherForUserId(userId));

        return publication.likes();
    }

    public abstract Publication publicationIdentifiedAs(String publicationId);

    protected abstract Stream<UserCard> userCardsStream();

    public abstract void start();

    public abstract void beginTransaction();

    public abstract void commitTransaction();

    public abstract void stop();

    public abstract void rollbackTransaction();

    public abstract void reset();
}
