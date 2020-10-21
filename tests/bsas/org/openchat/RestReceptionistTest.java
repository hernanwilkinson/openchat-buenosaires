package bsas.org.openchat;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import org.junit.jupiter.api.Test;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.eclipse.jetty.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.*;

public class RestReceptionistTest {

    private final TestObjectsBucket testObjects = new TestObjectsBucket();

    @Test
    public void canRegisterUserWithValidData() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));

        ReceptionistResponse response = receptionist.registerUser(juanPerezRegistrationBodyAsJson());

        assertJuanPerezOk(response, CREATED_201);
    }

    @Test
    public void returns400WithDuplicatedUser() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));

        receptionist.registerUser(juanPerezRegistrationBodyAsJson());
        ReceptionistResponse response = receptionist.registerUser(juanPerezRegistrationBodyAsJson());

        assertTrue(response.isStatus(BAD_REQUEST_400));
        assertEquals(OpenChatSystem.CANNOT_REGISTER_SAME_USER_TWICE,response.responseBody());
    }
    @Test
    public void validLoginsReturns200WithUserData() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));
        receptionist.registerUser(juanPerezRegistrationBodyAsJson());

        ReceptionistResponse response = receptionist.login(juanPerezLoginBodyAsJson());

        assertJuanPerezOk(response, OK_200);
    }
    @Test
    public void loginOfRegisteredUserReturns400WithInvalidCredentials() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));
        receptionist.registerUser(juanPerezRegistrationBodyAsJson());

        final JsonObject invalidJuanPerezLoginBodyAsJson = juanPerezLoginBodyAsJson();
        invalidJuanPerezLoginBodyAsJson.add(RestReceptionist.PASSWORD_KEY,TestObjectsBucket.JUAN_PEREZ_PASSWORD+"x");

        ReceptionistResponse response = receptionist.login(invalidJuanPerezLoginBodyAsJson);

        assertTrue(response.isStatus(NOT_FOUND_404));
        assertEquals(RestReceptionist.INVALID_CREDENTIALS,response.responseBody());
    }
    @Test
    public void usersReturns200WithAllRegisteredUsers() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));
        receptionist.registerUser(juanPerezRegistrationBodyAsJson());

        ReceptionistResponse response = receptionist.users();

        assertTrue(response.isStatus(OK_200));

        JsonArray responseBody = response.responseBodyAsJsonArray();
        assertEquals(1,responseBody.size());
        JsonObject userJson = responseBody.values().get(0).asObject();
        assertJuanPerezJson(userJson);
    }
    @Test
    public void followingsReturns201WhenFollowerCanFollowFollowee() {
        makePepeSanchezFollowJuanPerezAndAssert(
                (receptionist,response,followingsBody,followerResponse,followeeResponse)-> {
                    assertTrue(response.isStatus(CREATED_201));
                    assertEquals(RestReceptionist.FOLLOWING_CREATED,response.responseBody()); });
    }

    interface FollowingsAssertion {
        void accept(RestReceptionist receptionist, ReceptionistResponse response,
                    JsonObject followingsBodyAsJson,ReceptionistResponse followerResponse,
                    ReceptionistResponse followeeResponse);
    }
    private void makePepeSanchezFollowJuanPerezAndAssert(
           FollowingsAssertion assertions) {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));
        ReceptionistResponse followerResponse = receptionist.registerUser(pepeSanchezRegistrationBodyAsJson());
        ReceptionistResponse followeeResponse = receptionist.registerUser(juanPerezRegistrationBodyAsJson());

        JsonObject followingsBodyAsJson = new JsonObject()
                .add(RestReceptionist.FOLLOWER_ID_KEY, followerResponse.responseBodyAsJson().getString(RestReceptionist.ID_KEY,""))
                .add(RestReceptionist.FOLLOWEE_ID_KEY, followeeResponse.responseBodyAsJson().getString(RestReceptionist.ID_KEY,""));

        ReceptionistResponse response = receptionist.followings(followingsBodyAsJson);
        assertions.accept(receptionist,response,followingsBodyAsJson,followerResponse,followeeResponse);
    }

    @Test
    public void followingsReturns400WhenAlreadyFollowing() {
        makePepeSanchezFollowJuanPerezAndAssert(
                (receptionist,firstResponse,followingsBodyAsJson,followerResponse,followeeResponse)-> {
                    ReceptionistResponse response = receptionist.followings(followingsBodyAsJson);

                    assertTrue(response.isStatus(BAD_REQUEST_400));
                    assertEquals(Publisher.CANNOT_FOLLOW_TWICE,response.responseBody()); });
    }
    @Test
    public void followeesReturns200WithUserFollowees() {
        makePepeSanchezFollowJuanPerezAndAssert(
                (receptionist,firstResponse,followingsBody,followerResponse,followeeResponse)-> {
                    ReceptionistResponse response = receptionist.followeesOf(followerResponse.responseBodyAsJson().getString(RestReceptionist.ID_KEY,""));

                    assertTrue(response.isStatus(OK_200));

                    JsonArray responseBody = response.responseBodyAsJsonArray();
                    assertEquals(1,responseBody.size());
                    JsonObject userJson = responseBody.values().get(0).asObject();
                    assertJuanPerezJson(userJson);});
    }
    @Test
    public void publishReturns200WithPublicationInfo() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));
        ReceptionistResponse registeredUserResponse = receptionist.registerUser(juanPerezRegistrationBodyAsJson());

        final String publicationMessage = "hello";
        final String registeredUserId = registeredUserResponse.responseBodyAsJson().getString(RestReceptionist.ID_KEY, "");
        ReceptionistResponse publicationResponse = receptionist.addPublication(
                registeredUserId,
                messageBodyAsJsonFor(publicationMessage));

        assertTrue(publicationResponse.isStatus(CREATED_201));
        JsonObject responseBody = publicationResponse.responseBodyAsJson();
        assertFalse(responseBody.getString(RestReceptionist.POST_ID_KEY,"").isBlank());
        assertEquals(registeredUserId, responseBody.getString(RestReceptionist.USER_ID_KEY,""));
        assertEquals(publicationMessage, responseBody.getString(RestReceptionist.TEXT_KEY,""));
        assertEquals(formattedNow(),responseBody.getString(RestReceptionist.DATE_TIME_KEY,""));
    }
    @Test
    public void publishReturns400WithInappropriateWords() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));
        ReceptionistResponse registeredUserResponse = receptionist.registerUser(juanPerezRegistrationBodyAsJson());

        final String registeredUserId = registeredUserResponse.responseBodyAsJson().getString(RestReceptionist.ID_KEY, "");
        ReceptionistResponse publicationResponse = receptionist.addPublication(
                registeredUserId,
                messageBodyAsJsonFor("elephant"));

        assertTrue(publicationResponse.isStatus(BAD_REQUEST_400));
        assertEquals(Publication.INAPPROPRIATE_WORD,publicationResponse.responseBody());
    }
    @Test
    public void invalidUserCanNotPublishReturns() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));

        ReceptionistResponse publicationResponse = receptionist.addPublication(
                "",
                messageBodyAsJsonFor("something"));

        assertTrue(publicationResponse.isStatus(BAD_REQUEST_400));
        assertEquals(RestReceptionist.INVALID_CREDENTIALS,publicationResponse.responseBody());
    }

    private JsonObject messageBodyAsJsonFor(String message) {
        return new JsonObject()
                .add(RestReceptionist.TEXT_KEY, message);
    }

    @Test
    public void timelineReturns200WithUserPublications() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));
        ReceptionistResponse registeredUserResponse = receptionist.registerUser(juanPerezRegistrationBodyAsJson());

        JsonObject messageBodyAsJson = messageBodyAsJsonFor("Hello");
        final String followerId = registeredUserResponse.responseBodyAsJson().getString(RestReceptionist.ID_KEY, "");
        ReceptionistResponse publicationResponse = receptionist.addPublication(
                followerId,
                messageBodyAsJson);

        ReceptionistResponse timelineResponse = receptionist.timelineOf(followerId);
        assertTrue(timelineResponse.isStatus(OK_200));
        JsonArray timelineBody = timelineResponse.responseBodyAsJsonArray();
        assertEquals(1,timelineBody.size());

        JsonObject publicationAsJson = publicationResponse.responseBodyAsJson();
        JsonObject timelinePublicationAsJson = timelineBody.get(0).asObject();
        assertEquals(publicationAsJson,timelinePublicationAsJson);
    }
    @Test
    public void wallReturns200WithFollowerAndFolloweePublications() {
        makePepeSanchezFollowJuanPerezAndAssert(
                (receptionist,firstResponse,followingsBody,followerResponse,followeeResponse)-> {

            JsonObject followerMessageBodyAsJson = messageBodyAsJsonFor("Hello");
            final String followerId = followerResponse.responseBodyAsJson().getString(RestReceptionist.ID_KEY, "");
            ReceptionistResponse followerPublicationResponse = receptionist.addPublication(
                    followerId,
                    followerMessageBodyAsJson);
            JsonObject followeeMessageBodyAsJson = messageBodyAsJsonFor("Bye");
            final String followeeId = followeeResponse.responseBodyAsJson().getString(RestReceptionist.ID_KEY, "");
            ReceptionistResponse followeePublicationResponse = receptionist.addPublication(
                    followeeId,
                    followeeMessageBodyAsJson);

            ReceptionistResponse wallInfo = receptionist.wallOf(followerId);
            assertTrue(wallInfo.isStatus(OK_200));
            JsonArray timelineBody = wallInfo.responseBodyAsJsonArray();
            assertEquals(2,timelineBody.size());

            JsonObject followerPublicationAsJson = followerPublicationResponse.responseBodyAsJson();
            JsonObject wallFirstPublicationAsJson = timelineBody.get(0).asObject();
            assertEquals(followerPublicationAsJson,wallFirstPublicationAsJson);

            JsonObject followeePublicationAsJson = followeePublicationResponse.responseBodyAsJson();
            JsonObject wallSecondPublicationAsJson = timelineBody.get(1).asObject();
            assertEquals(followeePublicationAsJson,wallSecondPublicationAsJson);});
    }

    private String formattedNow() {
        return ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").format(testObjects.fixedNowClock().now());
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

    private JsonObject juanPerezRegistrationBodyAsJson() {
        return juanPerezLoginBodyAsJson()
                .add(RestReceptionist.ABOUT_KEY, TestObjectsBucket.JUAN_PEREZ_ABOUT);
    }

    private JsonObject pepeSanchezRegistrationBodyAsJson() {
        return pepeSanchezLoginBodyAsJson()
                .add(RestReceptionist.ABOUT_KEY, TestObjectsBucket.PEPE_SANCHEZ_ABOUT);
    }

    private void assertJuanPerezOk(ReceptionistResponse response, int status) {
        assertTrue(response.isStatus(status));
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
}