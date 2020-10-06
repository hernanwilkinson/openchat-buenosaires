public class Publisher {
    public static final String INVALID_NAME = "Name can not be blank";

    public static Publisher named(String name, String password, String about) {
        if(name.isBlank()) throw new RuntimeException(INVALID_NAME);
        return new Publisher();
    }

    public boolean isNamed(String potentialName) {
        return true;
    }
}
