package bsas.org.openchat;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.eclipse.jetty.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.*;

public class RestReceptionistTest {

    @Test
    public void canRegisterUserWithValidData() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem());

        ReceptionistResponse response = receptionist.registerUser(juanPerezRegistrationBody());

        assertJuanPerezOk(response);
    }

    private void assertJuanPerezOk(ReceptionistResponse response) {
        assertTrue(response.isStatus(CREATED_201));
        JsonObject responseBodyAsJson = response.responseBodyAsJson();
        assertJuanPerezJson(responseBodyAsJson);
    }

    private void assertJuanPerezJson(JsonObject responseBodyAsJson) {
        assertFalse(responseBodyAsJson.getString(RestReceptionist.ID_KEY, "").isBlank());
        assertEquals(TestObjectsBucket.JUAN_PEREZ_NAME, responseBodyAsJson.getString(RestReceptionist.USERNAME_KEY, ""));
        assertEquals(TestObjectsBucket.JUAN_PEREZ_ABOUT, responseBodyAsJson.getString(RestReceptionist.ABOUT_KEY, ""));
        assertEquals(
                TestObjectsBucket.JUAN_PEREZ_PASSWORD + "x",
                responseBodyAsJson.getString(RestReceptionist.PASSWORD_KEY, TestObjectsBucket.JUAN_PEREZ_PASSWORD + "x"));
    }

    private String juanPerezRegistrationBody() {
        return juanPerezLoginBodyAsJson()
                .add(RestReceptionist.ABOUT_KEY, TestObjectsBucket.JUAN_PEREZ_ABOUT)
                .toString();
    }
    private String pepeSanchezRegistrationBody() {
        return pepeSanchezLoginBodyAsJson()
                .add(RestReceptionist.ABOUT_KEY, TestObjectsBucket.PEPE_SANCHEZ_ABOUT)
                .toString();
    }
    @Test
    public void returns400WithDuplicatedUser() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem());

        receptionist.registerUser(juanPerezRegistrationBody());
        ReceptionistResponse response = receptionist.registerUser(juanPerezRegistrationBody());

        assertTrue(response.isStatus(BAD_REQUEST_400));
        assertEquals(OpenChatSystem.CANNOT_REGISTER_SAME_USER_TWICE,response.responseBody());
    }
    @Test
    public void validLoginsReturns200WithUserData() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem());
        receptionist.registerUser(juanPerezRegistrationBody());

        ReceptionistResponse response = receptionist.login(juanPerezLoginBodyAsJson().toString());

        assertJuanPerezOk(response);
    }
    @Test
    public void loginOfRegisteredUserReturns400WithInvalidCredentials() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem());
        receptionist.registerUser(juanPerezRegistrationBody());

        final JsonObject juanPerezLoginBodyAsJson = juanPerezLoginBodyAsJson();
        juanPerezLoginBodyAsJson.add(RestReceptionist.PASSWORD_KEY,TestObjectsBucket.JUAN_PEREZ_PASSWORD+"x");

        ReceptionistResponse response = receptionist.login(juanPerezLoginBodyAsJson.toString());

        assertTrue(response.isStatus(NOT_FOUND_404));
        assertEquals(RestReceptionist.INVALID_CREDENTIALS,response.responseBody());
    }
    @Test
    public void usersReturns200WithAllRegisteredUsers() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem());
        receptionist.registerUser(juanPerezRegistrationBody());

        ReceptionistResponse response = receptionist.users();

        assertTrue(response.isStatus(OK_200));

        JsonArray responseBody = response.responseBodyAsJsonArray();
        assertEquals(1,responseBody.size());
        JsonObject userJson = responseBody.values().get(0).asObject();
        assertJuanPerezJson(userJson);
    }
    @Test
    public void followingsReturns201WhenFollowerCanFollowFollowee() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem());
        ReceptionistResponse followerReturnInfo = receptionist.registerUser(juanPerezRegistrationBody());
        ReceptionistResponse followeeReturnInfo = receptionist.registerUser(pepeSanchezRegistrationBody());

        String followinsBody = new JsonObject()
                .add(RestReceptionist.FOLLOWER_ID, followerReturnInfo.responseBodyAsJson().getString(RestReceptionist.ID_KEY,""))
                .add(RestReceptionist.FOLLOWEE_ID, followeeReturnInfo.responseBodyAsJson().getString(RestReceptionist.ID_KEY,""))
                .toString();

        ReceptionistResponse response = receptionist.followings(followinsBody);

        assertTrue(response.isStatus(CREATED_201));
        assertEquals(RestReceptionist.FOLLOWING_CREATED,response.responseBody());
    }
    @Test
    public void followingsReturns400WhenAlreadyFollowing() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem());
        ReceptionistResponse followerReturnInfo = receptionist.registerUser(juanPerezRegistrationBody());
        ReceptionistResponse followeeReturnInfo = receptionist.registerUser(pepeSanchezRegistrationBody());

        String followinsBody = new JsonObject()
                .add(RestReceptionist.FOLLOWER_ID, followerReturnInfo.responseBodyAsJson().getString(RestReceptionist.ID_KEY,""))
                .add(RestReceptionist.FOLLOWEE_ID, followeeReturnInfo.responseBodyAsJson().getString(RestReceptionist.ID_KEY,""))
                .toString();

        receptionist.followings(followinsBody);
        ReceptionistResponse response = receptionist.followings(followinsBody);

        assertTrue(response.isStatus(BAD_REQUEST_400));
        assertEquals(Publisher.CANNOT_FOLLOW_TWICE,response.responseBody());
    }
    @Test
    public void followeesReturns200WithUserFollowees() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem());
        ReceptionistResponse followerReturnInfo = receptionist.registerUser(pepeSanchezRegistrationBody());
        ReceptionistResponse followeeReturnInfo = receptionist.registerUser(juanPerezRegistrationBody());

        String followinsBody = new JsonObject()
                .add(RestReceptionist.FOLLOWER_ID, followerReturnInfo.responseBodyAsJson().getString(RestReceptionist.ID_KEY,""))
                .add(RestReceptionist.FOLLOWEE_ID, followeeReturnInfo.responseBodyAsJson().getString(RestReceptionist.ID_KEY,""))
                .toString();

        receptionist.followings(followinsBody);
        ReceptionistResponse response = receptionist.followeesOf(followerReturnInfo.responseBodyAsJson().getString(RestReceptionist.ID_KEY,""));

        assertTrue(response.isStatus(OK_200));

        JsonArray responseBody = response.responseBodyAsJsonArray();
        assertEquals(1,responseBody.size());
        JsonObject userJson = responseBody.values().get(0).asObject();
        assertJuanPerezJson(userJson);
    }
    private JsonObject juanPerezLoginBodyAsJson() {
        return new JsonObject()
                .add(RestReceptionist.USERNAME_KEY, TestObjectsBucket.JUAN_PEREZ_NAME)
                .add(RestReceptionist.PASSWORD_KEY, TestObjectsBucket.JUAN_PEREZ_PASSWORD);
    }
    private JsonObject pepeSanchezLoginBodyAsJson() {
        return new JsonObject()
                .add(RestReceptionist.USERNAME_KEY, TestObjectsBucket.PEPE_SANCHEZ_NAME)
                .add(RestReceptionist.PASSWORD_KEY, TestObjectsBucket.PEPE_SANCHEZ_PASSWORD);
    }
}