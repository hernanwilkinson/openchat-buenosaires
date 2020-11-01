package bsas.org.openchat;

public class User {
    public static final String NAME_CANNOT_BE_BLANK = "Name can not be blank";

    private final String name;
    private final String about;
    private final String homePage;

    public static User named(String name, String about, String homePage) {
        assertNameIsNotBlank(name);

        return new User(name, about,homePage);
    }

    static void assertNameIsNotBlank(String name) {
        if(name.isBlank()) throw new ModelException(NAME_CANNOT_BE_BLANK);
    }

    private User(String name, String about, String homePage) {
        this.name = name;
        this.about = about;
        this.homePage = homePage;
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

    public String homePage() {
        return homePage;
    }
}