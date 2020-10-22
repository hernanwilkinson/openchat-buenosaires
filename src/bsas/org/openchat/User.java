package bsas.org.openchat;

public class User {
    public static final String NAME_CANNOT_BE_BLANK = "Name can not be blank";

    private final String name;
    private final String about;

    public static User named(String name, String password, String about) {
        assertNameIsNotBlank(name);

        return new User(name, about);
    }

    static void assertNameIsNotBlank(String name) {
        if(name.isBlank()) throw new ModelException(NAME_CANNOT_BE_BLANK);
    }

    private User(String name, String about) {
        this.name = name;
        this.about = about;
    }

    public boolean isNamed(String potentialName) {
        return name.equals(potentialName);
    }

    public String name() {
        return name;
    }

    public String about() {
        return about;
    }
}