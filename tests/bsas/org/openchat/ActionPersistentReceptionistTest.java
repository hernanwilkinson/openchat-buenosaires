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
        LineNumberReader reader = new LineNumberReader(new StringReader(writer.toString()));
        JsonObject savedJson = Json.parse(reader.readLine()).asObject();

        assertEquals(
                ActionPersistentReceptionist.REGISTER_USER_ACTION_NAME,
                savedJson.getString(ActionPersistentReceptionist.ACTION_NAME_KEY,null));
        assertEquals(
                registrationBodyAsJson,
                savedJson.get(ActionPersistentReceptionist.PARAMETERS_KEY).asObject());
        assertEquals(
                registrationResponse.responseBodyAsJson(),
                savedJson.get(ActionPersistentReceptionist.RETURN_KEY).asObject());
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
        LineNumberReader reader = new LineNumberReader(new StringReader(writer.toString()));
        reader.readLine();
        assertNull(reader.readLine());
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

        LineNumberReader reader = new LineNumberReader(new StringReader(writer.toString()));
        reader.readLine();
        reader.readLine();

        JsonObject savedJson = Json.parse(reader.readLine()).asObject();

        assertEquals(
                ActionPersistentReceptionist.FOLLOWINGS_ACTION_NAME,
                savedJson.getString(ActionPersistentReceptionist.ACTION_NAME_KEY,null));
        assertEquals(
                followingsBodyAsJson,
                savedJson.get(ActionPersistentReceptionist.PARAMETERS_KEY).asObject());
        assertEquals(
                new JsonObject(),
                savedJson.get(ActionPersistentReceptionist.RETURN_KEY).asObject());
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

        LineNumberReader reader = new LineNumberReader(new StringReader(writer.toString()));
        reader.readLine();
        reader.readLine();
        reader.readLine();

        assertNull(reader.readLine());
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

        LineNumberReader reader = new LineNumberReader(new StringReader(writer.toString()));
        reader.readLine();
        JsonObject savedJson = Json.parse(reader.readLine()).asObject();

        assertEquals(
                ActionPersistentReceptionist.ADD_PUBLICATION_ACTION_NAME,
                savedJson.getString(ActionPersistentReceptionist.ACTION_NAME_KEY,null));
        publicationAsJson.add(RestReceptionist.USER_ID_KEY,registrationResponse.idFromBody());
        assertEquals(
                publicationAsJson,
                savedJson.get(ActionPersistentReceptionist.PARAMETERS_KEY).asObject());
        assertEquals(
                publicationResponse.responseBodyAsJson(),
                savedJson.get(ActionPersistentReceptionist.RETURN_KEY).asObject());
    }

}