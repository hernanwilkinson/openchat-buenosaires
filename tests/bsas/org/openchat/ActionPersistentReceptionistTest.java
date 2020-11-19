package bsas.org.openchat;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ActionPersistentReceptionistTest {

    private TestObjectsBucket testObjectsBucket = new TestObjectsBucket();

    @Test
    public void persistsUserRegistration() throws IOException {
        final StringWriter writer = new StringWriter();
        ActionPersistentReceptionist receptionist = new ActionPersistentReceptionist(
                new RestReceptionist(new OpenChatSystem(()-> LocalDateTime.now())),
                writer);

        final JsonObject registrationBodyAsJson = testObjectsBucket.juanPerezRegistrationBodyAsJson();
        ReceptionistResponse registrationResponse = receptionist.registerUser(registrationBodyAsJson);
        assertActionInLineNumberIs(
                0, ActionPersistentReceptionist.REGISTER_USER_ACTION_NAME, registrationBodyAsJson, registrationResponse.responseBodyAsJson(), writer
        );
    }

    @Test
    public void invalidUserRegistrationIsNotPersisted() throws IOException {
        final StringWriter writer = new StringWriter();
        ActionPersistentReceptionist receptionist = new ActionPersistentReceptionist(
                new RestReceptionist(new OpenChatSystem(() -> LocalDateTime.now())),
                writer);

        final JsonObject registrationBodyAsJson = testObjectsBucket.juanPerezRegistrationBodyAsJson();
        receptionist.registerUser(registrationBodyAsJson);
        receptionist.registerUser(registrationBodyAsJson);
        assertNumberOfSavedActionsAre(1,writer);
    }

    @Test
    public void persistsFollowees() throws IOException {
        final StringWriter writer = new StringWriter();
        ActionPersistentReceptionist receptionist = new ActionPersistentReceptionist(
                new RestReceptionist(new OpenChatSystem(()-> LocalDateTime.now())),
                writer);

        ReceptionistResponse followerResponse = receptionist.registerUser(testObjectsBucket.pepeSanchezRegistrationBodyAsJson());
        ReceptionistResponse followeeResponse = receptionist.registerUser(testObjectsBucket.juanPerezRegistrationBodyAsJson());

        JsonObject followingsBodyAsJson = new JsonObject()
                .add(RestReceptionist.FOLLOWER_ID_KEY, followerResponse.idFromBody())
                .add(RestReceptionist.FOLLOWEE_ID_KEY, followeeResponse.idFromBody());
        receptionist.followings(followingsBodyAsJson);

        assertActionInLineNumberIs(2,
                ActionPersistentReceptionist.FOLLOWINGS_ACTION_NAME,
                followingsBodyAsJson,
                new JsonObject(),
                writer);
    }

    @Test
    public void invalidFollowingsIsNotPersisted() throws IOException {
        final StringWriter writer = new StringWriter();
        ActionPersistentReceptionist receptionist = new ActionPersistentReceptionist(
                new RestReceptionist(new OpenChatSystem(()-> LocalDateTime.now())),
                writer);

        ReceptionistResponse followerResponse = receptionist.registerUser(testObjectsBucket.pepeSanchezRegistrationBodyAsJson());
        ReceptionistResponse followeeResponse = receptionist.registerUser(testObjectsBucket.juanPerezRegistrationBodyAsJson());

        JsonObject followingsBodyAsJson = new JsonObject()
                .add(RestReceptionist.FOLLOWER_ID_KEY, followerResponse.idFromBody())
                .add(RestReceptionist.FOLLOWEE_ID_KEY, followeeResponse.idFromBody());
        receptionist.followings(followingsBodyAsJson);
        receptionist.followings(followingsBodyAsJson);

        assertNumberOfSavedActionsAre(3, writer);
    }

    @Test
    public void persistsPublications() throws IOException {
        final StringWriter writer = new StringWriter();
        ActionPersistentReceptionist receptionist = new ActionPersistentReceptionist(
                new RestReceptionist(new OpenChatSystem(()-> LocalDateTime.now())),
                writer);

        final JsonObject registrationBodyAsJson = testObjectsBucket.juanPerezRegistrationBodyAsJson();
        final ReceptionistResponse registrationResponse = receptionist.registerUser(registrationBodyAsJson);
        final JsonObject publicationAsJson = testObjectsBucket.publicationBodyAsJsonFor("hello");
        final ReceptionistResponse publicationResponse = receptionist.addPublication(
                registrationResponse.idFromBody(),
                publicationAsJson);

        assertActionInLineNumberIs(
                1,
                ActionPersistentReceptionist.ADD_PUBLICATION_ACTION_NAME,
                publicationAsJson.add(RestReceptionist.USER_ID_KEY,registrationResponse.idFromBody()),
                publicationResponse.responseBodyAsJson(),
                writer);
    }

    private void assertActionInLineNumberIs(int lineNumber, String actionName, JsonObject parameters, JsonObject returned, StringWriter writer) throws IOException {
        JsonObject savedJson = Json.parse(lineAt(lineNumber, writer)).asObject();

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

    private void assertNumberOfSavedActionsAre(int numberOfSavedActions, StringWriter writer) throws IOException {
        assertNull(lineAt(numberOfSavedActions, writer));
    }

    private String lineAt(int numberOfSavedActions, StringWriter writer) throws IOException {
        LineNumberReader reader = new LineNumberReader(new StringReader(writer.toString()));

        for (int currentLineNumber = 0; currentLineNumber < numberOfSavedActions; currentLineNumber++)
            reader.readLine();

        return reader.readLine();
    }
}