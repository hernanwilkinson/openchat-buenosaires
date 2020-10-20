import java.util.ArrayList;
import java.util.List;

public class OpenChatSystem {
    private final List<Publisher> users = new ArrayList<>();

    public boolean hasUsers() {
        return !users.isEmpty();
    }

    public void register(String userName, String password, String about) {
        users.add(Publisher.named(userName,password,about));
    }

    public boolean hasUserNamed(String potentialUserName) {
        return users.stream().anyMatch(user->user.isNamed(potentialUserName));
    }

    public int numberOfUsers() {
        return users.size();
    }
}
