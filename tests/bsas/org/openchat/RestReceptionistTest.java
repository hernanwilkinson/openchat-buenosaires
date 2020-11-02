package bsas.org.openchat;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import org.junit.jupiter.api.Test;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.eclipse.jetty.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.*;

public class RestReceptionistTest {

    private final TestObjectsBucket testObjects = new TestObjectsBucket();
    private RestReceptionist receptionist;

    @Test
    public void canRegisterUserWithValidData() {
        //No lo inicializo en el setUp por si quiero hacer restart
        //del contexto cuando debuggeo
        receptionist = createReceptionist();

        ReceptionistResponse response = registerJuanPerez();

        assertJuanPerezOk(response, CREATED_201);
    }
    @Test
    public void canNotRegisterDuplicatedUser() {
        receptionist = createReceptionist();

        registerJuanPerez();
        ReceptionistResponse response = registerJuanPerez();

        assertTrue(response.isStatus(BAD_REQUEST_400));
        assertEquals(OpenChatSystem.CANNOT_REGISTER_SAME_USER_TWICE,response.responseBody());
    }
    @Test
    public void canLoginRegisteredUserWithValidCredentials() {
        receptionist = createReceptionist();
        registerJuanPerez();

        ReceptionistResponse response = receptionist.login(juanPerezLoginBodyAsJson());

        assertJuanPerezOk(response, OK_200);
    }
    @Test
    public void canNotLoginWithInvalidCredentials() {
        receptionist = createReceptionist();
        registerJuanPerez();

        final JsonObject invalidJuanPerezLoginBodyAsJson = juanPerezLoginBodyAsJson();
        invalidJuanPerezLoginBodyAsJson
                .remove(RestReceptionist.PASSWORD_KEY)
                .add(RestReceptionist.PASSWORD_KEY,TestObjectsBucket.JUAN_PEREZ_PASSWORD+"x");

        ReceptionistResponse response = receptionist.login(invalidJuanPerezLoginBodyAsJson);

        assertTrue(response.isStatus(NOT_FOUND_404));
        assertEquals(RestReceptionist.INVALID_CREDENTIALS,response.responseBody());
    }
    @Test
    public void usersReturnsAllRegisteredUsers() {
        receptionist = createReceptionist();
        registerJuanPerez();

        ReceptionistResponse response = receptionist.users();

        assertIsArrayWithJuanPerezOnly(response);
    }

    @Test
    public void registeredUserCanFollowAnotherRegisteredUser() {
        makePepeSanchezFollowJuanPerezAndAssert(
            (receptionist,response,followingsBody,followerResponse,followeeResponse)-> {
                assertTrue(response.isStatus(CREATED_201));
                assertEquals(RestReceptionist.FOLLOWING_CREATED,response.responseBody()); });
    }

    @Test
    public void canNotFollowAnAlreadyFolloweeUser() {
        makePepeSanchezFollowJuanPerezAndAssert(
            (receptionist,firstResponse,followingsBodyAsJson,followerResponse,followeeResponse)-> {
                ReceptionistResponse response = receptionist.followings(followingsBodyAsJson);

                assertTrue(response.isStatus(BAD_REQUEST_400));
                assertEquals(Publisher.CANNOT_FOLLOW_TWICE,response.responseBody()); });
    }
    @Test
    public void followeesReturnsUserFollowees() {
        makePepeSanchezFollowJuanPerezAndAssert(
            (receptionist,firstResponse,followingsBody,followerResponse,followeeResponse)-> {
                ReceptionistResponse response = receptionist.followeesOf(idOfRegisteredUser(followerResponse));

                assertIsArrayWithJuanPerezOnly(response);
            });
    }
    @Test
    public void registeredUserCanPublishAppropriateMessage() {
        receptionist = createReceptionist();
        ReceptionistResponse registeredUserResponse = registerJuanPerez();

        final String publicationMessage = "hello";
        final String registeredUserId = idOfRegisteredUser(registeredUserResponse);
        ReceptionistResponse publicationResponse = receptionist.addPublication(
                registeredUserId,
                messageBodyAsJsonFor(publicationMessage));

        assertTrue(publicationResponse.isStatus(CREATED_201));
        JsonObject responseBody = publicationResponse.responseBodyAsJson();
        assertFalse(responseBody.getString(RestReceptionist.POST_ID_KEY,"").isBlank());
        assertEquals(registeredUserId, responseBody.getString(RestReceptionist.USER_ID_KEY,""));
        assertEquals(publicationMessage, responseBody.getString(RestReceptionist.TEXT_KEY,""));
        assertEquals(formattedNow(),responseBody.getString(RestReceptionist.DATE_TIME_KEY,""));
        assertEquals(0,responseBody.getInt(RestReceptionist.LIKES_KEY,-1));
    }

    @Test
    public void canNotPublishInappropriateWords() {
        receptionist = createReceptionist();
        ReceptionistResponse registeredUserResponse = registerJuanPerez();

        ReceptionistResponse publicationResponse = receptionist.addPublication(
                idOfRegisteredUser(registeredUserResponse),
                messageBodyAsJsonFor("elephant"));

        assertTrue(publicationResponse.isStatus(BAD_REQUEST_400));
        assertEquals(Publication.INAPPROPRIATE_WORD,publicationResponse.responseBody());
    }
    @Test
    public void invalidUserCanNotPublish() {
        receptionist = createReceptionist();

        final String invalidUserId = "";
        ReceptionistResponse publicationResponse = receptionist.addPublication(
                invalidUserId,
                messageBodyAsJsonFor("something"));

        assertTrue(publicationResponse.isStatus(BAD_REQUEST_400));
        assertEquals(RestReceptionist.INVALID_CREDENTIALS,publicationResponse.responseBody());
    }
    @Test
    public void timelineReturnsUserPublications() {
        receptionist = createReceptionist();
        ReceptionistResponse registeredUserResponse = registerJuanPerez();

        final String registereduserId = idOfRegisteredUser(registeredUserResponse);
        ReceptionistResponse publicationResponse = receptionist.addPublication(
                registereduserId,
                messageBodyAsJsonFor("Hello"));

        ReceptionistResponse timelineResponse = receptionist.timelineOf(registereduserId);
        assertTrue(timelineResponse.isStatus(OK_200));
        JsonArray timelineBody = timelineResponse.responseBodyAsJsonArray();
        assertEquals(1,timelineBody.size());

        JsonObject publicationAsJson = publicationResponse.responseBodyAsJson();
        JsonObject timelinePublicationAsJson = timelineBody.get(0).asObject();
        assertEquals(publicationAsJson,timelinePublicationAsJson);
    }
    @Test
    public void wallReturnsFollowerAndFolloweePublications() {
        makePepeSanchezFollowJuanPerezAndAssert(
            (receptionist,firstResponse,followingsBody,followerResponse,followeeResponse)-> {
                final String followerId = idOfRegisteredUser(followerResponse);
                ReceptionistResponse followerPublicationResponse = receptionist.addPublication(
                        followerId,
                        messageBodyAsJsonFor("Hello"));
                final String followeeId = idOfRegisteredUser(followeeResponse);
                ReceptionistResponse followeePublicationResponse = receptionist.addPublication(
                        followeeId,
                        messageBodyAsJsonFor("Bye"));

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
    @Test
    public void userCanLikePublication() {
        receptionist = createReceptionist();
        ReceptionistResponse publisherUserResponse = registerJuanPerez();
        ReceptionistResponse likerUserResponse = receptionist.registerUser(pepeSanchezRegistrationBodyAsJson());

        final String publisherId = idOfRegisteredUser(publisherUserResponse);
        ReceptionistResponse publicationResponse = receptionist.addPublication(
                publisherId,
                messageBodyAsJsonFor("Hello"));

        final JsonObject likerAsJson = new JsonObject()
                .add(RestReceptionist.USER_ID_KEY,idOfRegisteredUser(likerUserResponse));

        final String publicationId = publicationResponse.responseBodyAsJson().getString(RestReceptionist.POST_ID_KEY, "");
        ReceptionistResponse likeResponse = receptionist.likePublicationIdentifiedAs(
                publicationId,likerAsJson);

        assertTrue(likeResponse.isStatus(CREATED_201));
        JsonObject likesAsJson = likeResponse.responseBodyAsJson();
        assertEquals(1,likesAsJson.getInt(RestReceptionist.LIKES_KEY,-1));
    }

    private RestReceptionist createReceptionist() {
        return new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));
    }

    private ReceptionistResponse registerJuanPerez() {
        return receptionist.registerUser(juanPerezRegistrationBodyAsJson());
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
                .add(RestReceptionist.ABOUT_KEY, TestObjectsBucket.JUAN_PEREZ_ABOUT)
                .add(RestReceptionist.HOME_PAGE_KEY,TestObjectsBucket.JUAN_PEREZ_HOME_PAGE);
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
        assertEquals(TestObjectsBucket.JUAN_PEREZ_HOME_PAGE, responseBodyAsJson.getString(RestReceptionist.HOME_PAGE_KEY, ""));
        assertEquals(
                TestObjectsBucket.JUAN_PEREZ_PASSWORD + "x",
                responseBodyAsJson.getString(RestReceptionist.PASSWORD_KEY, TestObjectsBucket.JUAN_PEREZ_PASSWORD + "x"));
    }

    private void assertIsArrayWithJuanPerezOnly(ReceptionistResponse response) {
        assertTrue(response.isStatus(OK_200));
        JsonArray responseBody = response.responseBodyAsJsonArray();
        assertEquals(1, responseBody.size());
        JsonObject userJson = responseBody.values().get(0).asObject();
        assertJuanPerezJson(userJson);
    }

    interface FollowingsAssertion {
        void accept(RestReceptionist receptionist, ReceptionistResponse response,
                    JsonObject followingsBodyAsJson,ReceptionistResponse followerResponse,
                    ReceptionistResponse followeeResponse);
    }

    private void makePepeSanchezFollowJuanPerezAndAssert(
            FollowingsAssertion assertions) {
        receptionist = createReceptionist();
        ReceptionistResponse followerResponse = receptionist.registerUser(pepeSanchezRegistrationBodyAsJson());
        ReceptionistResponse followeeResponse = registerJuanPerez();

        JsonObject followingsBodyAsJson = new JsonObject()
                .add(RestReceptionist.FOLLOWER_ID_KEY, idOfRegisteredUser(followerResponse))
                .add(RestReceptionist.FOLLOWEE_ID_KEY, idOfRegisteredUser(followeeResponse));

        ReceptionistResponse response = receptionist.followings(followingsBodyAsJson);
        assertions.accept(receptionist,response,followingsBodyAsJson,followerResponse,followeeResponse);
    }

    private JsonObject messageBodyAsJsonFor(String message) {
        return new JsonObject()
                .add(RestReceptionist.TEXT_KEY, message);
    }

    private String idOfRegisteredUser(ReceptionistResponse registeredUserResponse) {
        return registeredUserResponse.responseBodyAsJson().getString(RestReceptionist.ID_KEY, "");
    }
}