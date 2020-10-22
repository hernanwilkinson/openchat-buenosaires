package bsas.org.openchat;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OpenChatSystem {
    public static final String CANNOT_REGISTER_SAME_USER_TWICE = "Username already in use.";
    public static final String USER_NOT_REGISTERED = "User not registered";

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
        userCards.put(userName,ValidUserCard.of(
                newUser,password, Publisher.relatedTo(newUser)));

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
                .getOrDefault(userName,InvalidUserCard.NULL_INSTANCE)
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
                .getOrDefault(userName,InvalidUserCard.NULL_INSTANCE)
                .withPublisherDo(publisherClosure);
    }

    public List<User> users() {
        return userCards.values().stream()
                .map(userCard->userCard.user())
                .collect(Collectors.toList());
    }

    private interface UserCard {
        boolean isOfUserNamed(String potentialUserName);
        boolean isPassword(String potentialPassword);
        <T> T ifValidPasswordDo(String potentialPassword,
            Function<User, T> authenticatedClosure, Supplier<T> failedClosure);
        <T> T withPublisherDo(Function<Publisher, T> publisherClosure);
        User user();
    }

    private static class ValidUserCard implements UserCard {
        private final User user;
        private final String password;
        private final Publisher publisher;

        public ValidUserCard(User user, String password, Publisher publisher) {
            this.user = user;
            this.password = password;
            this.publisher = publisher;
        }

        public static ValidUserCard of(User user, String password, Publisher publisher) {
            return new ValidUserCard(user,password,publisher);
        }

        @Override
        public boolean isOfUserNamed(String potentialUserName) {
            return user.isNamed(potentialUserName);
        }

        @Override
        public User user() {
            return user;
        }

        @Override
        public boolean isPassword(String potentialPassword) {
            return password.equals(potentialPassword);
        }

        @Override
        public <T> T ifValidPasswordDo(String potentialPassword,
                                        Function<User, T> authenticatedClosure, Supplier<T> failedClosure) {
            if( isPassword(potentialPassword))
                return authenticatedClosure.apply(user);
            else
                return failedClosure.get();
        }

        @Override
        public <T> T withPublisherDo(Function<Publisher, T> publisherClosure) {
            return publisherClosure.apply(publisher);
        }
    }

    private static class InvalidUserCard implements UserCard{

        public static final InvalidUserCard NULL_INSTANCE = new InvalidUserCard();

        @Override
        public boolean isOfUserNamed(String potentialUserName) {
            return false;
        }

        @Override
        public boolean isPassword(String potentialPassword) {
            return false;
        }

        @Override
        public <T> T ifValidPasswordDo(String potentialPassword, Function<User, T> authenticatedClosure, Supplier<T> failedClosure) {
            return failedClosure.get();
        }

        @Override
        public <T> T withPublisherDo(Function<Publisher, T> publisherClosure) {
            return throwUserNotRegistered();
        }

        @Override
        public User user() {
            return throwUserNotRegistered();
        }

        private <T> T throwUserNotRegistered() {
            throw new RuntimeException(OpenChatSystem.USER_NOT_REGISTERED);
        }
    }
}
