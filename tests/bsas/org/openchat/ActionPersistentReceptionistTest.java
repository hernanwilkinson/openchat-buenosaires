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
                registrationResponse.idFromBody(),
                savedJson.getString(ActionPersistentReceptionist.RETURN_KEY,null));
    }

    @Test
    public void invalidActionsAreNotPersisted() throws IOException {
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
}