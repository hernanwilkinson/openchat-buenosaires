package bsas.org.openchat;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.junit.jupiter.api.Assertions.*;

public class RestReceptionistTest {

    @Test
    public void canRegisterUserWithValidData() {
        RestReceptionist receptionist = new RestReceptionist();
        String registrationBody = new JsonObject()
                .add(RestReceptionist.USERNAME_KEY, TestObjectsBucket.JUAN_PEREZ_NAME)
                .add(RestReceptionist.PASSWORD_KEY, TestObjectsBucket.JUAN_PEREZ_PASSWORD)
                .add(RestReceptionist.ABOUT_KEY, TestObjectsBucket.JUAN_PEREZ_ABOUT)
                .toString();

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
}