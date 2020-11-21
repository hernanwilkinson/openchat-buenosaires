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

        ReceptionistResponse response = receptionist.login(testObjects.juanPerezLoginBodyAsJson());

        assertJuanPerezOk(response, OK_200);
    }
    @Test
    public void canNotLoginWithInvalidCredentials() {
        receptionist = createReceptionist();
        registerJuanPerez();

        final JsonObject invalidJuanPerezLoginBodyAsJson = testObjects.juanPerezLoginBodyAsJson();
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

        testObjects.assertIsArrayWithJuanPerezOnly(response);
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
                ReceptionistResponse response = receptionist.followeesOf(followerResponse.idFromBody());

                testObjects.assertIsArrayWithJuanPerezOnly(response);
            });
    }
    @Test
    public void registeredUserCanPublishAppropriateMessage() {
        receptionist = createReceptionist();
        ReceptionistResponse registeredUserResponse = registerJuanPerez();

        final String publicationMessage = "hello";
        ReceptionistResponse publicationResponse = publishMessageOf(
                registeredUserResponse,publicationMessage);

        assertTrue(publicationResponse.isStatus(CREATED_201));
        JsonObject responseBody = publicationResponse.responseBodyAsJson();
        assertFalse(responseBody.getString(RestReceptionist.POST_ID_KEY,"").isBlank());
        assertEquals(registeredUserResponse.idFromBody(), responseBody.getString(RestReceptionist.USER_ID_KEY,""));
        assertEquals(publicationMessage, responseBody.getString(RestReceptionist.TEXT_KEY,""));
        assertEquals(formattedNow(),responseBody.getString(RestReceptionist.DATE_TIME_KEY,""));
        assertEquals(0,responseBody.getInt(RestReceptionist.LIKES_KEY,-1));
    }

    @Test
    public void canNotPublishInappropriateWords() {
        receptionist = createReceptionist();
        ReceptionistResponse registeredUserResponse = registerJuanPerez();

        ReceptionistResponse publicationResponse = publishMessageOf(registeredUserResponse,"elephant");

        assertTrue(publicationResponse.isStatus(BAD_REQUEST_400));
        assertEquals(Publication.INAPPROPRIATE_WORD,publicationResponse.responseBody());
    }
    @Test
    public void invalidUserCanNotPublish() {
        receptionist = createReceptionist();

        final String invalidUserId = "";
        ReceptionistResponse publicationResponse = receptionist.addPublication(
                invalidUserId,
                testObjects.publicationBodyAsJsonFor("something"));

        assertTrue(publicationResponse.isStatus(BAD_REQUEST_400));
        assertEquals(RestReceptionist.INVALID_CREDENTIALS,publicationResponse.responseBody());
    }
    @Test
    public void timelineReturnsUserPublications() {
        receptionist = createReceptionist();
        ReceptionistResponse registeredUserResponse = registerJuanPerez();

        ReceptionistResponse publicationResponse = publishMessageOf(
                registeredUserResponse,"hello");

        ReceptionistResponse timelineResponse = receptionist.timelineOf(registeredUserResponse.idFromBody());
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
                ReceptionistResponse followerPublicationResponse = publishMessageOf(
                        followerResponse,"Hello");
                ReceptionistResponse followeePublicationResponse = publishMessageOf(
                        followeeResponse,"Bye");

                ReceptionistResponse wallInfo = receptionist.wallOf(followerResponse.idFromBody());
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
        receptionist = createReceptionist();
        ReceptionistResponse publisherUserResponse = registerJuanPerez();

        ReceptionistResponse publicationResponse = publishMessageOf(publisherUserResponse, "Hello");

        final JsonObject likerAsJson = new JsonObject()
                .add(RestReceptionist.USER_ID_KEY,"");

        ReceptionistResponse likeResponse = receptionist.likePublicationIdentifiedAs(
                publicationIdFrom(publicationResponse),likerAsJson);

        assertTrue(likeResponse.isStatus(BAD_REQUEST_400));
        assertEquals(RestReceptionist.INVALID_CREDENTIALS,likeResponse.responseBody());
    }
    @Test
    public void canNotLikeNotPublishedPublication() {
        receptionist = createReceptionist();
        ReceptionistResponse publisherUserResponse = registerJuanPerez();

        ReceptionistResponse likeResponse = receptionist.likePublicationIdentifiedAs(
                "", likerAsJsonFrom(publisherUserResponse));

        assertTrue(likeResponse.isStatus(BAD_REQUEST_400));
        assertEquals(RestReceptionist.INVALID_PUBLICATION,likeResponse.responseBody());
    }
    @Test
    public void timelineIncludesLikes() {
        receptionist = createReceptionist();
        ReceptionistResponse publisherUserResponse = registerJuanPerez();
        ReceptionistResponse likerUserResponse = registerPepeSanchez();

        ReceptionistResponse publicationResponse = publishMessageOf(publisherUserResponse, "Hello");

        receptionist.likePublicationIdentifiedAs(
                publicationIdFrom(publicationResponse),
                likerAsJsonFrom(likerUserResponse));

        ReceptionistResponse timelineResponse = receptionist.timelineOf(publisherUserResponse.idFromBody());
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

    private RestReceptionist createReceptionist() {
        return new RestReceptionist(new OpenChatSystem(testObjects.fixedNowClock()));
    }

    private ReceptionistResponse registerJuanPerez() {
        return receptionist.registerUser(testObjects.juanPerezRegistrationBodyAsJson());
    }

    private String formattedNow() {
        return ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").format(testObjects.fixedNowClock().now());
    }

    private void assertJuanPerezOk(ReceptionistResponse response, int status) {
        assertTrue(response.isStatus(status));
        JsonObject responseBodyAsJson = response.responseBodyAsJson();
        testObjects.assertJuanPerezJson(responseBodyAsJson);
    }

    interface FollowingsAssertion {
        void accept(RestReceptionist receptionist, ReceptionistResponse response,
                    JsonObject followingsBodyAsJson,ReceptionistResponse followerResponse,
                    ReceptionistResponse followeeResponse);
    }

    private void makePepeSanchezFollowJuanPerezAndAssert(
            FollowingsAssertion assertions) {
        receptionist = createReceptionist();
        ReceptionistResponse followerResponse = registerPepeSanchez();
        ReceptionistResponse followeeResponse = registerJuanPerez();

        JsonObject followingsBodyAsJson = new JsonObject()
                .add(RestReceptionist.FOLLOWER_ID_KEY, followerResponse.idFromBody())
                .add(RestReceptionist.FOLLOWEE_ID_KEY, followeeResponse.idFromBody());

        ReceptionistResponse response = receptionist.followings(followingsBodyAsJson);
        assertions.accept(receptionist,response,followingsBodyAsJson,followerResponse,followeeResponse);
    }

    private JsonObject likerAsJsonFrom(ReceptionistResponse likerUserResponse) {
        return new JsonObject()
                .add(RestReceptionist.USER_ID_KEY, likerUserResponse.idFromBody());
    }

    private ReceptionistResponse publishMessageOf(ReceptionistResponse publisherUserResponse, String message) {
        final String publisherId = publisherUserResponse.idFromBody();
        return receptionist.addPublication(
                publisherId,
                testObjects.publicationBodyAsJsonFor(message));
    }

    private ReceptionistResponse registerPepeSanchez() {
        return receptionist.registerUser(testObjects.pepeSanchezRegistrationBodyAsJson());
    }
}