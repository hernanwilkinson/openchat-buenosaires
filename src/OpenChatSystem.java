public class OpenChatSystem {
    private Publisher user;

    public boolean hasUsers() {
        return user!=null;
    }

    public void register(String userName, String password, String about) {
        user = Publisher.named(userName,password,about);
    }

    public boolean hasUserNamed(String potentialUserName) {
        return true;
    }

    public int numberOfUsers() {
        return 1;
    }
}
