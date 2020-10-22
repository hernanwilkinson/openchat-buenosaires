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
        userCards.put(userName, UserCard.of(
                newUser,password, Publisher.relatedTo(newUser)));

        return newUser;
    }

    private void assertIsNotDuplicated(String userName) {
        if(hasUserNamed(userName))
            throw new ModelException(CANNOT_REGISTER_SAME_USER_TWICE);
    }

    public boolean hasUserNamed(String potentialUserName) {
        return userCards.get(potentialUserName)!=null;
    }

    public int numberOfUsers() {
        return userCards.size();
    }

    public <T> T withAuthenticatedUserDo(String userName, String password,
                                         Function<User,T> authenticatedClosure, Supplier<T> failedClosure) {
        return withUserCardDo(userName,
                foundCard -> foundCard.ifValidPasswordDo(password,authenticatedClosure,failedClosure),
                failedClosure);
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

    public List<User> users() {
        return userCards.values().stream()
                .map(userCard->userCard.user())
                .collect(Collectors.toList());
    }

    private <T> T withUserCardDo(String userName,
                                 Function<UserCard,T> userCardAction, Supplier<T> failedClosure) {
        UserCard foundCard = userCards.get(userName);
        if(foundCard!=null)
            return userCardAction.apply(foundCard);
        else
            return failedClosure.get();
    }

    private <T> T withPublisherForUserNamed(String userName, Function<Publisher, T> publisherClosure) {
        return withUserCardDo(userName,
                foundCard -> foundCard.withPublisherDo(publisherClosure),
                ()-> {throw new ModelException(USER_NOT_REGISTERED);});
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

        public <T> T ifValidPasswordDo(String potentialPassword,
                                        Function<User, T> authenticatedClosure, Supplier<T> failedClosure) {
            if( isPassword(potentialPassword))
                return authenticatedClosure.apply(user);
            else
                return failedClosure.get();
        }

        public <T> T withPublisherDo(Function<Publisher, T> publisherClosure) {
            return publisherClosure.apply(publisher);
        }
    }
}
