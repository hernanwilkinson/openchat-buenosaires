package bsas.org.openchat;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

public class ActionPersistentReceptionistTest {

    private TestObjectsBucket testObjectsBucket;
    private StringWriter writer;
    private Receptionist receptionist;

    @BeforeEach
    public void setUp() throws NoSuchMethodException {
        testObjectsBucket = new TestObjectsBucket();
        writer = new StringWriter();
        receptionist = ActionPersistentReceptionist.asProxyOf(
                new RestReceptionist(new OpenChatSystem(testObjectsBucket.fixedNowClock())),
                writer);
    }

    @Test
    public void persistsUserRegistration() throws IOException {
        final JsonObject registrationBodyAsJson = testObjectsBucket.juanPerezRegistrationBodyAsJson();
        ReceptionistResponse registrationResponse = receptionist.registerUser(registrationBodyAsJson);

        assertActionInLineNumberIs(
                0,
                ActionPersistentReceptionist.REGISTER_USER_ACTION_NAME,
                registrationBodyAsJson,
                registrationResponse.responseBodyAsJson()
        );
    }

    @Test
    public void invalidUserRegistrationIsNotPersisted() throws IOException {
        final JsonObject registrationBodyAsJson = testObjectsBucket.juanPerezRegistrationBodyAsJson();
        receptionist.registerUser(registrationBodyAsJson);
        receptionist.registerUser(registrationBodyAsJson);

        assertNumberOfSavedActionsAre(1);
    }

    @Test
    public void persistsFollowees() throws IOException {
        JsonObject followingsBodyAsJson = makeJuanPerezFollowPepeSanchez();

        assertActionInLineNumberIs(2,
                ActionPersistentReceptionist.FOLLOWINGS_ACTION_NAME,
                followingsBodyAsJson,
                new JsonObject()
        );
    }

    @Test
    public void invalidFollowingsIsNotPersisted() throws IOException {
        JsonObject followingsBodyAsJson = makeJuanPerezFollowPepeSanchez();
        receptionist.followings(followingsBodyAsJson);

        assertNumberOfSavedActionsAre(3);
    }

    @Test
    public void persistsPublications() throws IOException {
        final JsonObject registrationBodyAsJson = testObjectsBucket.juanPerezRegistrationBodyAsJson();
        final ReceptionistResponse registrationResponse = receptionist.registerUser(registrationBodyAsJson);
        final JsonObject publicationAsJson = testObjectsBucket.publicationBodyAsJsonFor("hello");
        final ReceptionistResponse publicationResponse = receptionist.addPublication(
                registrationResponse.idFromBody(),
                publicationAsJson);

        assertActionInLineNumberIs(
                1,
                ActionPersistentReceptionist.ADD_PUBLICATION_ACTION_NAME,
                new JsonObject(publicationAsJson).add(RestReceptionist.USER_ID_KEY,registrationResponse.idFromBody()),
                publicationResponse.responseBodyAsJson()
        );
    }

    @Test
    public void persistsLikes() throws IOException {
        final JsonObject registrationBodyAsJson = testObjectsBucket.juanPerezRegistrationBodyAsJson();
        final ReceptionistResponse registrationResponse = receptionist.registerUser(registrationBodyAsJson);
        final JsonObject publicationAsJson = testObjectsBucket.publicationBodyAsJsonFor("hello");
        final ReceptionistResponse publicationResponse = receptionist.addPublication(
                registrationResponse.idFromBody(),
                publicationAsJson);

        final JsonObject likerJson = new JsonObject()
                .add(RestReceptionist.USER_ID_KEY, registrationResponse.idFromBody());
        final ReceptionistResponse likeResponse = receptionist.likePublicationIdentifiedAs(
                publicationResponse.postIdFromBody(),
                likerJson);

        assertActionInLineNumberIs(
                2,
                ActionPersistentReceptionist.LIKE_PUBLICATION_ACTION_NAME,
                new JsonObject(likerJson).add(RestReceptionist.POST_ID_KEY,publicationResponse.postIdFromBody()),
                likeResponse.responseBodyAsJson()
        );
    }

    @Test
    public void loginIsNotPersisted() throws IOException {
        receptionist.registerUser(testObjectsBucket.juanPerezRegistrationBodyAsJson());
        final ReceptionistResponse loginResponse = receptionist.login(testObjectsBucket.juanPerezLoginBodyAsJson());

        assertTrue(loginResponse.isStatus(HttpStatus.OK_200));
        assertNumberOfSavedActionsAre(1);
    }

    @Test
    public void usersIsNotPersisted() throws IOException {
        receptionist.registerUser(testObjectsBucket.juanPerezRegistrationBodyAsJson());
        final ReceptionistResponse usersResponse = receptionist.users();

        assertTrue(usersResponse.isStatus(HttpStatus.OK_200));
        assertNumberOfSavedActionsAre(1);
    }

    @Test
    public void followeesIsNotPersisted() throws IOException {
        final ReceptionistResponse registrationResponse = receptionist.registerUser(testObjectsBucket.juanPerezRegistrationBodyAsJson());
        final ReceptionistResponse followeesResponse = receptionist.followeesOf(registrationResponse.idFromBody());

        assertTrue(followeesResponse.isStatus(HttpStatus.OK_200));
        assertNumberOfSavedActionsAre(1);
    }

    @Test
    public void timelineIsNotPersisted() throws IOException {
        final ReceptionistResponse registrationResponse = receptionist.registerUser(testObjectsBucket.juanPerezRegistrationBodyAsJson());
        final ReceptionistResponse timelineResponse = receptionist.timelineOf(registrationResponse.idFromBody());

        assertTrue(timelineResponse.isStatus(HttpStatus.OK_200));
        assertNumberOfSavedActionsAre(1);
    }

    @Test
    public void wallOfIsNotPersisted() throws IOException {
        final ReceptionistResponse registrationResponse = receptionist.registerUser(testObjectsBucket.juanPerezRegistrationBodyAsJson());
        final ReceptionistResponse wallResponse = receptionist.wallOf(registrationResponse.idFromBody());

        assertTrue(wallResponse.isStatus(HttpStatus.OK_200));
        assertNumberOfSavedActionsAre(1);
    }

    @Test
    public void recoversRegisterUser() throws IOException {
        final ReceptionistResponse registrationResponse = receptionist.registerUser(testObjectsBucket.juanPerezRegistrationBodyAsJson());

        RestReceptionist recoveredReceptionist = PersistedReceptionistLoader.loadFrom(
                new StringReader(writer.toString()));

        final ReceptionistResponse usersResponse = recoveredReceptionist.users();
        testObjectsBucket.assertIsArrayWithJuanPerezOnly(usersResponse);
        JsonObject userJson = usersResponse.responseBodyAsJsonArray().values().get(0).asObject();
        assertEquals(registrationResponse.idFromBody(),userJson.getString(RestReceptionist.ID_KEY,null));
    }

    @Test
    public void recoversFollowings() throws IOException {
        final JsonObject followingsAsJson = makeJuanPerezFollowPepeSanchez();
        RestReceptionist recoveredReceptionist = PersistedReceptionistLoader.loadFrom(
                new StringReader(writer.toString()));

        final ReceptionistResponse followeesOfResponse = recoveredReceptionist.followeesOf(followingsAsJson.getString(RestReceptionist.FOLLOWER_ID_KEY,null));
        testObjectsBucket.assertIsArrayWithJuanPerezOnly(followeesOfResponse);
        JsonObject userJson = followeesOfResponse.responseBodyAsJsonArray().values().get(0).asObject();
        assertEquals(
                followingsAsJson.getString(RestReceptionist.FOLLOWEE_ID_KEY,null),
                userJson.getString(RestReceptionist.ID_KEY,null));
    }

    @Test
    public void recoversAddPublication() throws IOException {
        testObjectsBucket.changeNowTo(LocalDateTime.of(2020, Month.JANUARY,1,0,0));
        final JsonObject registrationBodyAsJson = testObjectsBucket.juanPerezRegistrationBodyAsJson();
        final ReceptionistResponse registrationResponse = receptionist.registerUser(registrationBodyAsJson);
        final ReceptionistResponse publicationResponse = receptionist.addPublication(
                registrationResponse.idFromBody(),
                testObjectsBucket.publicationBodyAsJsonFor("hello"));

        RestReceptionist recoveredReceptionist = PersistedReceptionistLoader.loadFrom(
                new StringReader(writer.toString()));

        final ReceptionistResponse timelineResponse = recoveredReceptionist.timelineOf(registrationResponse.idFromBody());

        JsonArray timelineBody = timelineResponse.responseBodyAsJsonArray();
        assertEquals(1,timelineBody.size());

        JsonObject publicationAsJson = publicationResponse.responseBodyAsJson();
        JsonObject restoredPublicationAsJson = timelineBody.get(0).asObject();
        assertEquals(publicationAsJson,restoredPublicationAsJson);
    }

    @Test
    public void recoversLikes() throws IOException {
        final JsonObject registrationBodyAsJson = testObjectsBucket.juanPerezRegistrationBodyAsJson();
        final ReceptionistResponse registrationResponse = receptionist.registerUser(registrationBodyAsJson);
        final ReceptionistResponse publicationResponse = receptionist.addPublication(
                registrationResponse.idFromBody(),
                testObjectsBucket.publicationBodyAsJsonFor("hello"));

        final JsonObject likerJson = new JsonObject()
                .add(RestReceptionist.USER_ID_KEY, registrationResponse.idFromBody());
        final ReceptionistResponse likeResponse = receptionist.likePublicationIdentifiedAs(
                publicationResponse.postIdFromBody(),
                likerJson);

        RestReceptionist recoveredReceptionist = PersistedReceptionistLoader.loadFrom(
                new StringReader(writer.toString()));

        final ReceptionistResponse timelineResponse = recoveredReceptionist.timelineOf(registrationResponse.idFromBody());

        JsonArray timelineBody = timelineResponse.responseBodyAsJsonArray();

        JsonObject publicationAsJson = publicationResponse.responseBodyAsJson();
        publicationAsJson
                .remove(RestReceptionist.LIKES_KEY)
                .add(RestReceptionist.LIKES_KEY,1);

        JsonObject restoredPublicationAsJson = timelineBody.get(0).asObject();
        assertEquals(publicationAsJson,restoredPublicationAsJson);
    }

    @Test
    public void failsGracefullyWhenLineHasNoJsonObject() throws IOException {

        writer.write("something went wrong");
        RuntimeException error = assertThrows(
                RuntimeException.class,
                ()-> PersistedReceptionistLoader.loadFrom(
                    new StringReader(writer.toString())));

        assertEquals(PersistedReceptionistLoader.invalidRecordErrorMessage(1), error.getMessage());
    }

    @Test
    public void failsGracefullyWhenNoParameters() throws IOException {

        writer.write(new JsonObject().toString());
        RuntimeException error = assertThrows(
                RuntimeException.class,
                ()-> PersistedReceptionistLoader.loadFrom(
                        new StringReader(writer.toString())));

        assertEquals(PersistedReceptionistLoader.invalidRecordErrorMessage(1), error.getMessage());
    }

    @Test
    public void failsGracefullyWhenNoReturnObject() throws IOException {

        writer.write(new JsonObject()
                .add(ActionPersistentReceptionist.PARAMETERS_KEY,new JsonObject())
                .toString());
        RuntimeException error = assertThrows(
                RuntimeException.class,
                ()-> PersistedReceptionistLoader.loadFrom(
                        new StringReader(writer.toString())));

        assertEquals(PersistedReceptionistLoader.invalidRecordErrorMessage(1), error.getMessage());
    }

    @Test
    public void failsGracefullyWhenInvalidActionName() throws IOException {

        writer.write(new JsonObject()
                .add(ActionPersistentReceptionist.PARAMETERS_KEY,new JsonObject())
                .add(ActionPersistentReceptionist.RETURN_KEY,new JsonObject())
                .add(ActionPersistentReceptionist.ACTION_NAME_KEY,"")
                .toString());
        RuntimeException error = assertThrows(
                RuntimeException.class,
                ()-> PersistedReceptionistLoader.loadFrom(
                        new StringReader(writer.toString())));

        assertEquals(PersistedReceptionistLoader.invalidRecordErrorMessage(1), error.getMessage());
    }

    @Test
    public void recoveredReceptionistGeneratesNewIdsAfterRecover() throws IOException {
        final ReceptionistResponse juanPerezRegistrationResponse = receptionist.registerUser(testObjectsBucket.juanPerezRegistrationBodyAsJson());

        RestReceptionist recoveredReceptionist = PersistedReceptionistLoader.loadFrom(
                new StringReader(writer.toString()));

        final ReceptionistResponse pepeSanchezRegistrationResponse = recoveredReceptionist.registerUser(testObjectsBucket.pepeSanchezRegistrationBodyAsJson());

        assertNotEquals(
                pepeSanchezRegistrationResponse.idFromBody(),
                juanPerezRegistrationResponse.idFromBody());
    }

    @Test
    public void recoveredReceptionistUsesNewNowAfterRecovered() throws IOException {
        testObjectsBucket.changeNowTo(LocalDateTime.of(2020, Month.JANUARY,1,0,0));
        final JsonObject registrationBodyAsJson = testObjectsBucket.juanPerezRegistrationBodyAsJson();
        final ReceptionistResponse registrationResponse = receptionist.registerUser(registrationBodyAsJson);
        final ReceptionistResponse publicationResponse = receptionist.addPublication(
                registrationResponse.idFromBody(),
                testObjectsBucket.publicationBodyAsJsonFor("hello"));

        RestReceptionist recoveredReceptionist = PersistedReceptionistLoader.loadFrom(
                new StringReader(writer.toString()));
        recoveredReceptionist.addPublication(
                registrationResponse.idFromBody(),
                testObjectsBucket.publicationBodyAsJsonFor("bye"));

        final ReceptionistResponse timelineResponse = recoveredReceptionist.timelineOf(registrationResponse.idFromBody());

        JsonArray timelineBody = timelineResponse.responseBodyAsJsonArray();
        JsonObject firstPublicationAsJson = timelineBody.get(0).asObject();
        JsonObject secondPublicationAsJson = timelineBody.get(1).asObject();

        assertNotEquals(
                firstPublicationAsJson.getString(RestReceptionist.DATE_TIME_KEY,null),
                secondPublicationAsJson.getString(RestReceptionist.DATE_TIME_KEY,null));

    }


    private void assertActionInLineNumberIs(int lineNumber, String actionName, JsonObject parameters, JsonObject returned) throws IOException {
        JsonObject savedJson = Json.parse(lineAt(lineNumber)).asObject();

        assertEquals(
                actionName,
                savedJson.getString(ActionPersistentReceptionist.ACTION_NAME_KEY,null));
        assertEquals(
                parameters,
                savedJson.get(ActionPersistentReceptionist.PARAMETERS_KEY).asObject());
        assertEquals(
                returned,
                savedJson.get(ActionPersistentReceptionist.RETURN_KEY).asObject());
    }

    private void assertNumberOfSavedActionsAre(int numberOfSavedActions) throws IOException {
        assertNull(lineAt(numberOfSavedActions));
    }

    private String lineAt(int numberOfSavedActions) throws IOException {
        LineNumberReader reader = new LineNumberReader(new StringReader(writer.toString()));

        for (int currentLineNumber = 0; currentLineNumber < numberOfSavedActions; currentLineNumber++)
            reader.readLine();

        return reader.readLine();
    }

    private JsonObject makeJuanPerezFollowPepeSanchez() {
        ReceptionistResponse followerResponse = receptionist.registerUser(testObjectsBucket.pepeSanchezRegistrationBodyAsJson());
        ReceptionistResponse followeeResponse = receptionist.registerUser(testObjectsBucket.juanPerezRegistrationBodyAsJson());

        JsonObject followingsBodyAsJson = new JsonObject()
                .add(RestReceptionist.FOLLOWER_ID_KEY, followerResponse.idFromBody())
                .add(RestReceptionist.FOLLOWEE_ID_KEY, followeeResponse.idFromBody());
        receptionist.followings(followingsBodyAsJson);

        return followingsBodyAsJson;
    }


}