public class User {
    public static final String NAME_CANNOT_BE_BLANK = "Name can not be blank";
    private final String name;

    private User(String name, String password, String about) {
        this.name = name;
    }

    public static User named(String name, String password, String about) {
        assertNameIsNotBlank(name);

        return new User(name,password,about);
    }

    static void assertNameIsNotBlank(String name) {
        if(name.isBlank()) throw new RuntimeException(NAME_CANNOT_BE_BLANK);
    }

    public boolean isNamed(String potentialName) {
        return name.equals(potentialName);
    }
}