package bsas.org.openchat;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OpenChatSystem {
    public static final String CANNOT_REGISTER_SAME_USER_TWICE = "Username already in use.";
    public static final String USER_NOT_REGISTERED = "User not registered";
    public static final String INVALID_PUBLICATION = "Invalid post";

    private final Map<String,UserCard> userCards = new HashMap<>();
    private final Map<Publication,Set<String>> likersByPublication = new HashMap<>();
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
                userName,
                UserCard.of(newUser,password, Publisher.relatedTo(newUser)));

        return newUser;
    }

    public boolean hasUserNamed(String potentialUserName) {
        //Uso userCardForUserName en vez de hacer userCards.get
        //para que la búsqueda por nombre esté en un solo lugar
        return userCardForUserName(potentialUserName).isPresent();
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

    public Publication publishForUserNamed(String userName, String message) {
        final Publication newPublication = publisherForUserNamed(userName).publish(message, clock.now());
        likersByPublication.put(newPublication,new HashSet<>());

        return newPublication;
    }

    public List<Publication> timeLineForUserNamed(String userName) {
        return publisherForUserNamed(userName).timeLine();
    }

    public void followForUserNamed(String followerUserName, String followeeUserName) {
        Publisher follower = publisherForUserNamed(followerUserName);
        Publisher followee = publisherForUserNamed(followeeUserName);

        follower.follow(followee);
    }

    public List<User> followeesOfUserNamed(String userName) {
        return publisherForUserNamed(userName).followees().stream()
                .map(publisher -> publisher.relatedUser())
                .collect(Collectors.toList());
    }

    public List<Publication> wallForUserNamed(String userName) {
        return publisherForUserNamed(userName).wall();
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
        return userCardForUserName(userName)
                .filter(foundCard -> foundCard.isPassword(password))
                .map(foundCard -> foundCard.user());
    }

    private Optional<UserCard> userCardForUserName(String userName) {
        return Optional.ofNullable(userCards.get(userName));
    }

    private Publisher publisherForUserNamed(String userName) {
        return userCardForUserName(userName)
                .map(userCard -> userCard.publisher())
                .orElseThrow(()-> new ModelException(USER_NOT_REGISTERED));
    }

    public int likesOf(Publication publication) {
        return likersOf(publication).size();
    }

    public int likePublication(Publication publication, String userName) {
        final Set<String> likers = likersOf(publication);

        likers.add(userName);

        return likers.size();
    }

    private Set<String> likersOf(Publication publication) {
        final Set<String> likers = likersByPublication.get(publication);
        if (likers == null) throw new ModelException(INVALID_PUBLICATION);

        return likers;
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
    }
}