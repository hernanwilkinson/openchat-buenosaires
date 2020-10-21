package bsas.org.openchat;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OpenChatSystem {
    public static final String CANNOT_REGISTER_SAME_USER_TWICE = "Username already in use.";
    public static final String USER_NOT_REGISTERED = "User not registered";

    private final List<User> users = new ArrayList<>();
    private final Map<User,String> passwordsByUser = new HashMap<>();
    private final Map<User,Publisher> publisherByUser = new HashMap<>();
    private final Map<String,UserCard> userCards = new HashMap<>();
    private final Clock clock;

    public OpenChatSystem(Clock clock){
        this.clock = clock;
    }

    public boolean hasUsers() {
        return !userCards.isEmpty();
    }

    public User register(String userName, String password, String about) {
        assertIsNotDuplicated(userName);

        final User newUser = User.named(userName, password, about);
        //users.add(newUser);
        passwordsByUser.put(newUser,password);
        final Publisher publisher = Publisher.relatedTo(newUser);
        publisherByUser.put(newUser, publisher);
        userCards.put(userName,UserCard.of(newUser,password,publisher));

        return newUser;
    }

    private void assertIsNotDuplicated(String userName) {
        if(hasUserNamed(userName))
            throw new RuntimeException(CANNOT_REGISTER_SAME_USER_TWICE);
    }

    public boolean hasUserNamed(String potentialUserName) {
        return userCards.get(potentialUserName)!=null;
    }

    public int numberOfUsers() {
        return userCards.size();
    }

    public <T> T withAuthenticatedUserDo(String userName, String password,
            Function<User,T> authenticatedClosure, Supplier<T> failedClosure) {
        return userCards
                .getOrDefault(userName,UserCard.NULL_USER_CARD)
                .ifValidPasswordDo(password,authenticatedClosure,failedClosure);
    }

    public Publication publishForUserNamed(String userName, String message) {
        return withPublisherForUserNamed(userName,
                publisher -> publisher.publish(message, clock.now()));
    }

    public List<Publication> timeLineForUserNamed(String userName) {
        return withPublisherForUserNamed(userName, publisher->publisher.timeLine());
    }

    public void followForUserNamed(String followerUserName, String followeeUserName) {
        withPublisherForUserNamed(followerUserName,
                follower->withPublisherForUserNamed(followeeUserName,
                        followee->{ follower.follow(followee); return 1;}));
    }

    public List<User> followeesOfUserNamed(String userName) {
        return withPublisherForUserNamed(userName,
                follower->follower.followees().stream()
                        .map(publisher->publisher.relatedUser())
                        .collect(Collectors.toList()));
    }

    public List<Publication> wallForUserNamed(String userName) {
        return withPublisherForUserNamed(
                userName,
                publisher->publisher.wall());
    }

    private <T> T withPublisherForUserNamed(String userName, Function<Publisher, T> publisherClosure) {
        return userCards
                .getOrDefault(userName,UserCard.NULL_USER_CARD)
                .withPublisherDo(publisherClosure);
    }

    public List<User> users() {
        return userCards.values().stream()
                .map(userCard->userCard.user())
                .collect(Collectors.toList());
    }

    private static class UserCard {
        public static UserCard NULL_USER_CARD = UserCard.createNullCard();

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

        private static UserCard createNullCard() {
            return of(null,null,null);
        }

        public boolean isOfUserNamed(String potentialUserName) {
            return user.isNamed(potentialUserName);
        }

        public User user() {
            return user;
        }

        public boolean isPassword(String potentialPassword) {
            return password!=null && password.equals(potentialPassword);
        }

        private <T> T ifValidPasswordDo(String potentialPassword,
            Function<User, T> authenticatedClosure, Supplier<T> failedClosure) {
            if( isPassword(potentialPassword))
                return authenticatedClosure.apply(user());
            else
                return failedClosure.get();
        }

        public <T> T withPublisherDo(Function<Publisher, T> publisherClosure) {
            if(publisher==null)
                throw new RuntimeException(USER_NOT_REGISTERED);

            return publisherClosure.apply(publisher);
        }
    }
}
