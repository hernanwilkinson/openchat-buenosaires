package bsas.org.openchat;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;

import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.junit.jupiter.api.Assertions.*;

public class TestObjectsBucket {
    public static final String PEPE_SANCHEZ_NAME = "Pepe Sanchez";
    public static final String PEPE_SANCHEZ_PASSWORD = "password";
    public static final String PEPE_SANCHEZ_ABOUT = "anotherAbout";
    public static final String PEPE_SANCHEZ_HOME_PAGE = "www.twitter.com/pepeSanchez";

    public static final String JUAN_PEREZ_NAME = "Juan Perez";
    public static final String JUAN_PEREZ_PASSWORD = "otherPassword";
    public static final String JUAN_PEREZ_ABOUT = "about";
    public static final String JUAN_PEREZ_HOME_PAGE = "www.twitter.com/juanPerez";

    private final LocalDateTime now = LocalDateTime.now();

    public static <T extends Throwable> void assertThrowsModelExceptionWithErrorMessage(
            Executable closureToFail, String errorMessage) {
        ModelException error = assertThrows(
                ModelException.class,
                closureToFail);

        assertEquals(errorMessage,error.getMessage());
    }

    public User createUserJuanPerez() {
        return User.named(
                JUAN_PEREZ_NAME,
                JUAN_PEREZ_ABOUT,
                JUAN_PEREZ_HOME_PAGE);
    }

    public User createPepeSanchez() {
        return User.named(
                PEPE_SANCHEZ_NAME,
                PEPE_SANCHEZ_ABOUT,
                PEPE_SANCHEZ_HOME_PAGE);
    }

    public Clock fixedNowClock(){
        return ()-> now;
    }

    public JsonObject juanPerezLoginBodyAsJson() {
        return new JsonObject()
                .add(RestReceptionist.USERNAME_KEY, JUAN_PEREZ_NAME)
                .add(RestReceptionist.PASSWORD_KEY, JUAN_PEREZ_PASSWORD);
    }

    public JsonObject juanPerezRegistrationBodyAsJson() {
        return juanPerezLoginBodyAsJson()
                .add(RestReceptionist.ABOUT_KEY, JUAN_PEREZ_ABOUT)
                .add(RestReceptionist.HOME_PAGE_KEY, JUAN_PEREZ_HOME_PAGE);
    }

    public JsonObject pepeSanchezLoginBodyAsJson() {
        return new JsonObject()
                .add(RestReceptionist.USERNAME_KEY, PEPE_SANCHEZ_NAME)
                .add(RestReceptionist.PASSWORD_KEY, PEPE_SANCHEZ_PASSWORD);
    }

    public JsonObject pepeSanchezRegistrationBodyAsJson() {
        return pepeSanchezLoginBodyAsJson()
                .add(RestReceptionist.ABOUT_KEY, PEPE_SANCHEZ_ABOUT);
    }

    public JsonObject publicationBodyAsJsonFor(String message) {
        return new JsonObject()
                .add(RestReceptionist.TEXT_KEY, message);
    }

    public void assertJuanPerezJson(JsonObject responseBodyAsJson) {
        assertFalse(responseBodyAsJson.getString(RestReceptionist.ID_KEY, "").isBlank());
        assertEquals(JUAN_PEREZ_NAME, responseBodyAsJson.getString(RestReceptionist.USERNAME_KEY, ""));
        assertEquals(JUAN_PEREZ_ABOUT, responseBodyAsJson.getString(RestReceptionist.ABOUT_KEY, ""));
        assertEquals(JUAN_PEREZ_HOME_PAGE, responseBodyAsJson.getString(RestReceptionist.HOME_PAGE_KEY, ""));
        assertEquals(
                JUAN_PEREZ_PASSWORD + "x",
                responseBodyAsJson.getString(RestReceptionist.PASSWORD_KEY, JUAN_PEREZ_PASSWORD + "x"));
    }

    public void assertIsArrayWithJuanPerezOnly(ReceptionistResponse response) {
        assertTrue(response.isStatus(OK_200));
        JsonArray responseBody = response.responseBodyAsJsonArray();
        assertEquals(1, responseBody.size());
        JsonObject userJson = responseBody.values().get(0).asObject();
        assertJuanPerezJson(userJson);
    }
}
