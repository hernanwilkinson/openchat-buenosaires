package org.openchat.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OpenChatSystem {
    public static final String CANNOT_REGISTER_SAME_USER_TWICE = "Cannot register same user twice";
    public static final String USER_NOT_REGISTERED = "org.openchat.model.User not registered";

    private final List<User> users = new ArrayList<>();
    private final Map<User,String> passwordsByUser = new HashMap<>();
    private final Map<User,Publisher> publisherByUser = new HashMap<>();

    public boolean hasUsers() {
        return !users.isEmpty();
    }

    public User register(String userName, String password, String about) {
        assertIsNotDuplicated(userName);

        final User newUser = User.named(userName, password, about);
        users.add(newUser);
        passwordsByUser.put(newUser,password);
        publisherByUser.put(newUser,Publisher.relatedTo(newUser));

        return newUser;
    }

    private void assertIsNotDuplicated(String userName) {
        if(hasUserNamed(userName)) throw new RuntimeException(CANNOT_REGISTER_SAME_USER_TWICE);
    }

    public boolean hasUserNamed(String potentialUserName) {
        return users.stream().anyMatch(user->user.isNamed(potentialUserName));
    }

    public int numberOfUsers() {
        return users.size();
    }

    public <T> T withAuthenticatedUserDo(String userName, String password,
            Function<User,T> authenticatedClosure, Supplier<T> failedClosure) {

        return users.stream()
                .filter(user->user.isNamed(userName))
                .findFirst()
                .map(user->ifValidPasswordDo(user,password,authenticatedClosure,failedClosure))
                .orElseGet(failedClosure);
    }

    public Publication publishForUserNamed(String userName, String message) {
        return withPublisherForUserNamed(userName,
                publisher->publisher.publish(message, LocalDateTime.now()));
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

    private <T> T ifValidPasswordDo(User user, String password,
                                    Function<User,T> authenticatedClosure, Supplier<T> failedClosure) {
        if(passwordsByUser.get(user).equals(password))
            return authenticatedClosure.apply(user);
        else
            return failedClosure.get();
    }

    private <T> T withPublisherForUserNamed(String userName, Function<Publisher, T> publisherClosure) {
        return users.stream().
                filter(user->user.isNamed(userName))
                .findFirst()
                .map(user-> publisherClosure.apply(publisherByUser.get(user)))
                .orElseThrow(()->new RuntimeException(USER_NOT_REGISTERED));
    }

}
