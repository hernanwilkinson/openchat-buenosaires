package bsas.org.openchat;

import java.util.UUID;

public class User {
    public static final String NAME_CANNOT_BE_BLANK = "Name can not be blank";

    private final String name;
    private final String about;
    private final String homePage;
    private final String restId;

    public static User named(String name, String about, String homePage) {
        return named(name, about, homePage, UUID.randomUUID().toString());
    }

    public static User named(String name, String about, String homePage, String restId) {
        assertNameIsNotBlank(name);

        return new User(name, about,homePage,restId);
    }

    static void assertNameIsNotBlank(String name) {
        if(name.isBlank()) throw new ModelException(NAME_CANNOT_BE_BLANK);
    }

    private User(String name, String about, String homePage, String restId) {
        this.name = name;
        this.about = about;
        this.homePage = homePage;
        this.restId = restId;
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

    public String restId() {
        return restId;
    }
}