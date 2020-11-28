package bsas.org.openchat;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.eclipse.jetty.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.*;

public class RestReceptionistTest {

    private TestObjectsBucket testObjects;
    private RestReceptionist receptionist;
    private OpenChatSystem system;

    @BeforeEach
    public void setUp() {
        testObjects = new TestObjectsBucket();
        system = DevelopmentEnvironment.current().createSystem(testObjects.fixedNowClock());
        receptionist = new RestReceptionist(system);
    }

    @AfterEach
    public void tearDown(){
        system.stop();
    }

    @Test
    public void canRegisterUserWithValidData() {
        ReceptionistResponse response = registerJuanPerez();

        assertJuanPerezOk(response, CREATED_201);
    }
    @Test
    public void canNotRegisterDuplicatedUser() {
        registerJuanPerez();
        ReceptionistResponse response = registerJuanPerez();

        assertTrue(response.isStatus(BAD_REQUEST_400));
        assertEquals(OpenChatSystem.CANNOT_REGISTER_SAME_USER_TWICE,response.responseBody());
    }
    @Test
    public void canLoginRegisteredUserWithValidCredentials() {
        registerJuanPerez();

        ReceptionistResponse response = receptionist.login(juanPerezLoginBodyAsJson());

        assertJuanPerezOk(response, OK_200);
    }
    @Test
    public void canNotLoginWithInvalidCredentials() {
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
        registerJuanPerez();

        ReceptionistResponse response = receptionist.users();

        assertIsArrayWithJuanPerezOnly(response);
    }

    @Test
    public void registeredUserCanFollowAnotherRegisteredUser() {
        makePepeSanchezFollowJuanPerezAndAssert(
            (receptionist,response,followingsBody,followedResponse,followerResponse)-> {
                assertTrue(response.isStatus(CREATED_201));
                assertEquals(RestReceptionist.FOLLOWING_CREATED,response.responseBody()); });
    }

    @Test
    public void canNotFollowAnAlreadyFollowerUser() {
        makePepeSanchezFollowJuanPerezAndAssert(
            (receptionist,firstResponse,followingsBodyAsJson,followedResponse,followerResponse)-> {
                ReceptionistResponse response = receptionist.followings(followingsBodyAsJson);

                assertTrue(response.isStatus(BAD_REQUEST_400));
                assertEquals(Publisher.CANNOT_FOLLOW_TWICE,response.responseBody()); });
    }
    @Test
    public void followersReturnsUserFollowers() {
        makePepeSanchezFollowJuanPerezAndAssert(
            (receptionist,firstResponse,followingsBody,followedResponse,followerResponse)-> {
                ReceptionistResponse response = receptionist.followersOf(idOfRegisteredUser(followedResponse));

                assertIsArrayWithJuanPerezOnly(response);
            });
    }
    @Test
    public void registeredUserCanPublishAppropriateMessage() {
        ReceptionistResponse registeredUserResponse = registerJuanPerez();

        final String publicationMessage = "hello";
        ReceptionistResponse publicationResponse = publishMessageOf(
                registeredUserResponse,publicationMessage);

        assertTrue(publicationResponse.isStatus(CREATED_201));
        JsonObject responseBody = publicationResponse.responseBodyAsJson();
        assertFalse(responseBody.getString(RestReceptionist.POST_ID_KEY,"").isBlank());
        assertEquals(idOfRegisteredUser(registeredUserResponse), responseBody.getString(RestReceptionist.USER_ID_KEY,""));
        assertEquals(publicationMessage, responseBody.getString(RestReceptionist.TEXT_KEY,""));
        assertEquals(formattedNow(),responseBody.getString(RestReceptionist.DATE_TIME_KEY,""));
        assertEquals(0,responseBody.getInt(RestReceptionist.LIKES_KEY,-1));
    }

    @Test
    public void canNotPublishInappropriateWords() {
        ReceptionistResponse registeredUserResponse = registerJuanPerez();

        ReceptionistResponse publicationResponse = publishMessageOf(registeredUserResponse,"elephant");

        assertTrue(publicationResponse.isStatus(BAD_REQUEST_400));
        assertEquals(Publication.INAPPROPRIATE_WORD,publicationResponse.responseBody());
    }
    @Test
    public void invalidUserCanNotPublish() {
        final String invalidUserId = UUID.randomUUID().toString();
        ReceptionistResponse publicationResponse = receptionist.addPublication(
                invalidUserId,
                messageBodyAsJsonFor("something"));

        assertTrue(publicationResponse.isStatus(BAD_REQUEST_400));
        assertEquals(OpenChatSystem.USER_NOT_REGISTERED,publicationResponse.responseBody());
    }
    @Test
    public void timelineReturnsUserPublications() {
        ReceptionistResponse registeredUserResponse = registerJuanPerez();

        ReceptionistResponse publicationResponse = publishMessageOf(
                registeredUserResponse,"hello");

        ReceptionistResponse timelineResponse = receptionist.timelineOf(idOfRegisteredUser(registeredUserResponse));
        assertTrue(timelineResponse.isStatus(OK_200));
        JsonArray timelineBody = timelineResponse.responseBodyAsJsonArray();
        assertEquals(1,timelineBody.size());

        JsonObject publicationAsJson = publicationResponse.responseBodyAsJson();
        JsonObject timelinePublicationAsJson = timelineBody.get(0).asObject();
        assertEquals(publicationAsJson,timelinePublicationAsJson);
    }
    @Test
    public void wallReturnsFollowedAndFollowerPublications() {
        makePepeSanchezFollowJuanPerezAndAssert(
            (receptionist,firstResponse,followingsBody,followedResponse,followerResponse)-> {
                ReceptionistResponse followedPublicationResponse = publishMessageOf(
                        followedResponse,"Hello");
                ReceptionistResponse followerPublicationResponse = publishMessageOf(
                        followerResponse,"Bye");

                ReceptionistResponse wallInfo = receptionist.wallOf(idOfRegisteredUser(followedResponse));
                assertTrue(wallInfo.isStatus(OK_200));
                JsonArray timelineBody = wallInfo.responseBodyAsJsonArray();
                assertEquals(2,timelineBody.size());

                JsonObject followedPublicationAsJson = followedPublicationResponse.responseBodyAsJson();
                JsonObject wallFirstPublicationAsJson = timelineBody.get(0).asObject();
                assertEquals(followedPublicationAsJson,wallFirstPublicationAsJson);

                JsonObject followerPublicationAsJson = followerPublicationResponse.responseBodyAsJson();
                JsonObject wallSecondPublicationAsJson = timelineBody.get(1).asObject();
                assertEquals(followerPublicationAsJson,wallSecondPublicationAsJson);});
    }
    @Test
    public void userCanLikePublication() {
        ReceptionistResponse publisherUserResponse = registerJuanPerez();
        ReceptionistResponse likerUserResponse = registerPepeSanchez();

        ReceptionistResponse publicationResponse = publishMessageOf(
                publisherUserResponse, "Hello");

        ReceptionistResponse likeResponse = receptionist.likePublicationIdentifiedAs(
                publicationIdFrom(publicationResponse),
                likerAsJsonFrom(likerUserResponse));

        assertTrue(likeResponse.isStatus(OK_200));
        JsonObject likesAsJson = likeResponse.responseBodyAsJson();
        assertEquals(1,likesAsJson.getInt(RestReceptionist.LIKES_KEY,-1));
    }

    @Test
    public void notRegisteredUserCanNotLikePublication() {
        ReceptionistResponse publisherUserResponse = registerJuanPerez();

        ReceptionistResponse publicationResponse = publishMessageOf(publisherUserResponse, "Hello");

        final JsonObject likerAsJson = new JsonObject()
                .add(RestReceptionist.USER_ID_KEY,"");

        ReceptionistResponse likeResponse = receptionist.likePublicationIdentifiedAs(
                publicationIdFrom(publicationResponse),likerAsJson);

        assertTrue(likeResponse.isStatus(BAD_REQUEST_400));
        assertEquals(OpenChatSystem.USER_NOT_REGISTERED,likeResponse.responseBody());
    }
    @Test
    public void canNotLikeNotPublishedPublication() {
        ReceptionistResponse publisherUserResponse = registerJuanPerez();

        ReceptionistResponse likeResponse = receptionist.likePublicationIdentifiedAs(
                "", likerAsJsonFrom(publisherUserResponse));

        assertTrue(likeResponse.isStatus(BAD_REQUEST_400));
        assertEquals(RestReceptionist.INVALID_PUBLICATION,likeResponse.responseBody());
    }
    @Test
    public void timelineIncludesLikes() {
        ReceptionistResponse publisherUserResponse = registerJuanPerez();
        ReceptionistResponse likerUserResponse = registerPepeSanchez();

        ReceptionistResponse publicationResponse = publishMessageOf(publisherUserResponse, "Hello");

        receptionist.likePublicationIdentifiedAs(
                publicationIdFrom(publicationResponse),
                likerAsJsonFrom(likerUserResponse));

        ReceptionistResponse timelineResponse = receptionist.timelineOf(idOfRegisteredUser(publisherUserResponse));
        assertTrue(timelineResponse.isStatus(OK_200));
        JsonArray timelineBody = timelineResponse.responseBodyAsJsonArray();
        assertEquals(1,timelineBody.size());

        JsonObject publicationAsJson = publicationResponse.responseBodyAsJson();
        JsonObject timelinePublicationAsJson = timelineBody.get(0).asObject();
        assertEquals(1,timelinePublicationAsJson.getInt(RestReceptionist.LIKES_KEY,-1));
    }

    private String publicationIdFrom(ReceptionistResponse publicationResponse) {
        return publicationResponse.responseBodyAsJson().getString(RestReceptionist.POST_ID_KEY, "");
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
                    JsonObject followingsBodyAsJson,ReceptionistResponse followedResponse,
                    ReceptionistResponse followerResponse);
    }

    private void makePepeSanchezFollowJuanPerezAndAssert(
            FollowingsAssertion assertions) {
        ReceptionistResponse followedResponse = registerPepeSanchez();
        ReceptionistResponse followerResponse = registerJuanPerez();

        JsonObject followingsBodyAsJson = new JsonObject()
                .add(RestReceptionist.FOLLOWED_ID_KEY, idOfRegisteredUser(followedResponse))
                .add(RestReceptionist.FOLLOWER_ID_KEY, idOfRegisteredUser(followerResponse));

        ReceptionistResponse response = receptionist.followings(followingsBodyAsJson);
        assertions.accept(receptionist,response,followingsBodyAsJson,followedResponse,followerResponse);
    }

    private JsonObject messageBodyAsJsonFor(String message) {
        return new JsonObject()
                .add(RestReceptionist.TEXT_KEY, message);
    }

    private String idOfRegisteredUser(ReceptionistResponse registeredUserResponse) {
        return registeredUserResponse.responseBodyAsJson().getString(RestReceptionist.ID_KEY, "");
    }

    private JsonObject likerAsJsonFrom(ReceptionistResponse likerUserResponse) {
        return new JsonObject()
                .add(RestReceptionist.USER_ID_KEY,idOfRegisteredUser(likerUserResponse));
    }

    private ReceptionistResponse publishMessageOf(ReceptionistResponse publisherUserResponse, String message) {
        final String publisherId = idOfRegisteredUser(publisherUserResponse);
        return receptionist.addPublication(
                publisherId,
                messageBodyAsJsonFor(message));
    }

    private ReceptionistResponse registerPepeSanchez() {
        return receptionist.registerUser(pepeSanchezRegistrationBodyAsJson());
    }
}