import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class OpenChatSystem {
    public static final String CANNOT_REGISTER_SAME_USER_TWICE = "Cannot register same user twice";
    private final List<User> users = new ArrayList<>();
    private final Map<User,String> passwordsByUser = new HashMap<>();
    private final Map<User,Publisher> publisherByUser = new HashMap<>();

    public boolean hasUsers() {
        return !users.isEmpty();
    }

    public void register(String userName, String password, String about) {
        assertIsNotDuplicated(userName);

        final User newUser = User.named(userName, password, about);
        users.add(newUser);
        passwordsByUser.put(newUser,password);
        publisherByUser.put(newUser,Publisher.relatedTo(newUser));
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

    public void withAuthenticatedUserDo(String userName, String password,
            Consumer<User> authenticatedClosure, Runnable failedClosure) {
        users.stream()
                .filter(user->user.isNamed(userName))
                .findFirst()
                .ifPresentOrElse(
                        user->ifValidPasswordDo(user, password, authenticatedClosure, failedClosure),
                        failedClosure
                );
    }

    private void ifValidPasswordDo(User user, String password, Consumer<User> authenticatedClosure, Runnable failedClosure) {
        if(passwordsByUser.get(user).equals(password))
            authenticatedClosure.accept(user);
        else
            failedClosure.run();
    }

    public Publication publishForUserNamed(String userName, String message) {
        User foundUser = users.stream().
                filter(user->user.isNamed(userName))
                .findFirst()
                .get();

        return publisherByUser.get(foundUser).publish(message,LocalDateTime.now());
    }

    public List<Publication> timeLineForUserNamed(String userName) {
        User foundUser = users.stream().
                filter(user->user.isNamed(userName))
                .findFirst()
                .get();

        return publisherByUser.get(foundUser).timeLine();
    }

}
