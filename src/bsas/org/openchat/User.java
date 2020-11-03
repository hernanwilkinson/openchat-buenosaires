package bsas.org.openchat;

public class User {
    public static final String NAME_CANNOT_BE_BLANK = "Name can not be blank";

    private final String name;
    private final String about;
    private final String url;

    //TODO: Borrar
    public static User named(String name, String password, String about) {
        return named(name,password,about,"not defined");
    }

    public static User named(String name, String password, String about, String url) {
        assertNameIsNotBlank(name);

        return new User(name, about, url);
    }

    static void assertNameIsNotBlank(String name) {
        if(name.isBlank()) throw new ModelException(NAME_CANNOT_BE_BLANK);
    }

    private User(String name, String about, String url) {
        this.name = name;
        this.about = about;
        this.url = url;
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

    public String url() {
        return url;
    }
}