package bsas.org.openchat;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import org.junit.jupiter.api.Test;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.eclipse.jetty.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.*;

public class RestReceptionistTest {

    private TestObjectsBucket testObjects = new TestObjectsBucket();

    @Test
    public void canRegisterUserWithValidData() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));

        ReceptionistResponse response = receptionist.registerUser(juanPerezRegistrationAsJson());

        assertJuanPerezOk(response, CREATED_201);
    }

    private JsonObject juanPerezRegistrationAsJson() {
        return juanPerezLoginBodyAsJson()
                .add(RestReceptionist.ABOUT_KEY, TestObjectsBucket.JUAN_PEREZ_ABOUT);
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

    @Test
    public void returns400WithDuplicatedUser() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));

        receptionist.registerUser(juanPerezRegistrationAsJson());
        ReceptionistResponse response = receptionist.registerUser(juanPerezRegistrationAsJson());

        assertTrue(response.isStatus(BAD_REQUEST_400));
        assertEquals(OpenChatSystem.CANNOT_REGISTER_SAME_USER_TWICE,response.responseBody());
    }
    @Test
    public void validLoginsReturns200WithUserData() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));
        receptionist.registerUser(juanPerezRegistrationAsJson());

        ReceptionistResponse response = receptionist.login(juanPerezLoginBodyAsJson().toString());

        assertJuanPerezOk(response, OK_200);
    }
    @Test
    public void loginOfRegisteredUserReturns400WithInvalidCredentials() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));
        receptionist.registerUser(juanPerezRegistrationAsJson());

        final JsonObject juanPerezLoginBodyAsJson = juanPerezLoginBodyAsJson();
        juanPerezLoginBodyAsJson.add(RestReceptionist.PASSWORD_KEY,TestObjectsBucket.JUAN_PEREZ_PASSWORD+"x");

        ReceptionistResponse response = receptionist.login(juanPerezLoginBodyAsJson.toString());

        assertTrue(response.isStatus(NOT_FOUND_404));
        assertEquals(RestReceptionist.INVALID_CREDENTIALS,response.responseBody());
    }
    @Test
    public void usersReturns200WithAllRegisteredUsers() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));
        receptionist.registerUser(juanPerezRegistrationAsJson());

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
                    String followinsBody,ReceptionistResponse followerResponse,
                    ReceptionistResponse followeeResponse);
    }
    private void makePepeSanchezFollowJuanPerezAndAssert(
           FollowingsAssertion assertions) {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));
        ReceptionistResponse followerResponse = receptionist.registerUser(pepeSanchezRegistrationAsJson());
        ReceptionistResponse followeeResponse = receptionist.registerUser(juanPerezRegistrationAsJson());

        String followinsBody = new JsonObject()
                .add(RestReceptionist.FOLLOWER_ID, followerResponse.responseBodyAsJson().getString(RestReceptionist.ID_KEY,""))
                .add(RestReceptionist.FOLLOWEE_ID, followeeResponse.responseBodyAsJson().getString(RestReceptionist.ID_KEY,""))
                .toString();

        ReceptionistResponse response = receptionist.followings(followinsBody);
        assertions.accept(receptionist,response,followinsBody,followerResponse,followeeResponse);
    }

    private JsonObject pepeSanchezRegistrationAsJson() {
        return pepeSanchezLoginBodyAsJson()
                .add(RestReceptionist.ABOUT_KEY, TestObjectsBucket.PEPE_SANCHEZ_ABOUT);
    }

    @Test
    public void followingsReturns400WhenAlreadyFollowing() {
        makePepeSanchezFollowJuanPerezAndAssert(
                (receptionist,firstResponse,followingsBody,followerResponse,followeeReponse)-> {
                    ReceptionistResponse response = receptionist.followings(followingsBody);

                    assertTrue(response.isStatus(BAD_REQUEST_400));
                    assertEquals(Publisher.CANNOT_FOLLOW_TWICE,response.responseBody()); });
    }
    @Test
    public void followeesReturns200WithUserFollowees() {
        makePepeSanchezFollowJuanPerezAndAssert(
                (receptionist,firstResponse,followingsBody,followerResponse,followeeReponse)-> {
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
        ReceptionistResponse registeredUserResponse = receptionist.registerUser(juanPerezRegistrationAsJson());

        final String publicationMessage = "hello";
        String messageBody = messageBodyFor(publicationMessage);
        final String registeredUserId = registeredUserResponse.responseBodyAsJson().getString(RestReceptionist.ID_KEY, "");
        ReceptionistResponse publicationInfo = receptionist.addPublication(
                registeredUserId,
                messageBody);

        assertTrue(publicationInfo.isStatus(CREATED_201));
        JsonObject responseBody = publicationInfo.responseBodyAsJson();
        assertFalse(responseBody.getString(RestReceptionist.POST_ID_KEY,"").isBlank());
        assertEquals(registeredUserId, responseBody.getString(RestReceptionist.USER_ID_KEY,""));
        assertEquals(publicationMessage, responseBody.getString(RestReceptionist.TEXT_KEY,""));
        assertEquals(formattedNow(),responseBody.getString(RestReceptionist.DATE_TIME_KEY,""));
    }
    @Test
    public void publishReturns400WithInappropriateWords() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));
        ReceptionistResponse registeredUserResponse = receptionist.registerUser(juanPerezRegistrationAsJson());

        String messageBody = messageBodyFor("elephant");
        final String registeredUserId = registeredUserResponse.responseBodyAsJson().getString(RestReceptionist.ID_KEY, "");
        ReceptionistResponse publicationInfo = receptionist.addPublication(
                registeredUserId,
                messageBody);

        assertTrue(publicationInfo.isStatus(BAD_REQUEST_400));
        assertEquals(Publication.INAPPROPRIATE_WORD,publicationInfo.responseBody());
    }

    private String messageBodyFor(String message) {
        final String publicationMessage = message;
        String messageBody = new JsonObject()
                .add(RestReceptionist.TEXT_KEY, publicationMessage)
                .toString();
        return messageBody;
    }

    @Test
    public void timelineReturns200WithUserPublications() {
        RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));
        ReceptionistResponse registeredUserResponse = receptionist.registerUser(juanPerezRegistrationAsJson());

        String messageBody = messageBodyFor("Hello");
        final String followerId = registeredUserResponse.responseBodyAsJson().getString(RestReceptionist.ID_KEY, "");
        ReceptionistResponse publicationResponse = receptionist.addPublication(
                followerId,
                messageBody);

        ReceptionistResponse timelineResponse = receptionist.timelineOf(followerId);
        assertTrue(timelineResponse.isStatus(OK_200));
        JsonArray timelineBody = timelineResponse.responseBodyAsJsonArray();
        assertEquals(1,timelineBody.size());

        JsonObject publicationAsJson = publicationResponse.responseBodyAsJson();
        JsonObject timelinePublicationAsJson = timelineBody.get(0).asObject();
        assertEquals(publicationAsJson,timelinePublicationAsJson);
    }
    @Test
    public void wallReturns200WithFollwerAndFolloweePublications() {
        makePepeSanchezFollowJuanPerezAndAssert(
                (receptionist,firstResponse,followingsBody,followerResponse,followeeResponse)-> {

            String followerMessageBody = messageBodyFor("Hello");
            final String followerId = followerResponse.responseBodyAsJson().getString(RestReceptionist.ID_KEY, "");
            ReceptionistResponse followerPublicationResponse = receptionist.addPublication(
                    followerId,
                    followerMessageBody);
            String followeeMessageBody = messageBodyFor("Bye");
            final String followeeId = followeeResponse.responseBodyAsJson().getString(RestReceptionist.ID_KEY, "");
            ReceptionistResponse followeePublicationResponse = receptionist.addPublication(
                    followeeId,
                    followeeMessageBody);

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
}