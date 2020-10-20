package bsas.org.openchat;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.eclipse.jetty.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.*;

public class RestReceptionistTest {

    @Test
    public void canRegisterUserWithValidData() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem());
        String registrationBody = juanPerezRegistrationBody();

        ReceptionistResponse response = receptionist.registerUser(registrationBody);

        assertTrue(response.isStatus(CREATED_201));
        JsonObject responseBodyAsJson = Json.parse(response.responseBody()).asObject();
        assertFalse(responseBodyAsJson.getString(RestReceptionist.ID_KEY,"").isBlank());
        assertEquals(TestObjectsBucket.JUAN_PEREZ_NAME,responseBodyAsJson.getString(RestReceptionist.USERNAME_KEY,""));
        assertEquals(TestObjectsBucket.JUAN_PEREZ_ABOUT,responseBodyAsJson.getString(RestReceptionist.ABOUT_KEY,""));
        assertEquals(
                TestObjectsBucket.JUAN_PEREZ_PASSWORD+"x",
                responseBodyAsJson.getString(RestReceptionist.PASSWORD_KEY,TestObjectsBucket.JUAN_PEREZ_PASSWORD+"x"));
    }

    private String juanPerezRegistrationBody() {
        return new JsonObject()
                .add(RestReceptionist.USERNAME_KEY, TestObjectsBucket.JUAN_PEREZ_NAME)
                .add(RestReceptionist.PASSWORD_KEY, TestObjectsBucket.JUAN_PEREZ_PASSWORD)
                .add(RestReceptionist.ABOUT_KEY, TestObjectsBucket.JUAN_PEREZ_ABOUT)
                .toString();
    }
    @Test
    public void returns400WithDuplicatedUser() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem());
        String registrationBody = juanPerezRegistrationBody();

        receptionist.registerUser(registrationBody);
        ReceptionistResponse response = receptionist.registerUser(registrationBody);

        assertTrue(response.isStatus(BAD_REQUEST_400));
        assertEquals(OpenChatSystem.CANNOT_REGISTER_SAME_USER_TWICE,response.responseBody());
    }
    @Test
    public void validLoginsReturns200WithUserData() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem());
        String registrationBody = juanPerezRegistrationBody();

        receptionist.registerUser(registrationBody);
        String loginBody = new JsonObject()
                .add(RestReceptionist.USERNAME_KEY, TestObjectsBucket.JUAN_PEREZ_NAME)
                .add(RestReceptionist.PASSWORD_KEY, TestObjectsBucket.JUAN_PEREZ_PASSWORD)
                .toString();

        ReceptionistResponse response = receptionist.login(loginBody);

        assertTrue(response.isStatus(OK_200));
        JsonObject responseBodyAsJson = Json.parse(response.responseBody()).asObject();
        assertFalse(responseBodyAsJson.getString(RestReceptionist.ID_KEY,"").isBlank());
        assertEquals(TestObjectsBucket.JUAN_PEREZ_NAME,responseBodyAsJson.getString(RestReceptionist.USERNAME_KEY,""));
        assertEquals(TestObjectsBucket.JUAN_PEREZ_ABOUT,responseBodyAsJson.getString(RestReceptionist.ABOUT_KEY,""));
        assertEquals(
                TestObjectsBucket.JUAN_PEREZ_PASSWORD+"x",
                responseBodyAsJson.getString(RestReceptionist.PASSWORD_KEY,TestObjectsBucket.JUAN_PEREZ_PASSWORD+"x"));
    }
}