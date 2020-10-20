import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class OpenChatSystem {
    public static final String CANNOT_REGISTER_SAME_USER_TWICE = "Cannot register same user twice";
    private final List<User> users = new ArrayList<>();

    public boolean hasUsers() {
        return !users.isEmpty();
    }

    public void register(String userName, String password, String about) {
        assertIsNotDupilcated(userName);

        users.add(User.named(userName,password,about));
    }

    private void assertIsNotDupilcated(String userName) {
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
        authenticatedClosure.accept(users.get(0));
    }
}
