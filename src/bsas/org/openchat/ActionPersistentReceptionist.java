package bsas.org.openchat;

import com.eclipsesource.json.JsonObject;

import java.io.StringWriter;

public class ActionPersistentReceptionist implements Receptionist{
    private final RestReceptionist receptionist;
    private final StringWriter writer;

    public ActionPersistentReceptionist(RestReceptionist receptionist, StringWriter writer) {
        this.receptionist = receptionist;
        this.writer = writer;
    }

    @Override
    public ReceptionistResponse registerUser(JsonObject registrationBodyAsJson) {
        final ReceptionistResponse response = receptionist.registerUser(registrationBodyAsJson);
        JsonObject actionAsJson = new JsonObject()
                .add("actionName", "registerUser")
                .add("parameters",registrationBodyAsJson)
                .add("return",response.responseBodyAsJson().getString(RestReceptionist.ID_KEY,""));

        writer.write(actionAsJson.toString());
        writer.write("\n");

        return response;
    }

    @Override
    public ReceptionistResponse login(JsonObject loginBodyAsJson) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReceptionistResponse users() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReceptionistResponse followings(JsonObject followingsBodyAsJson) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReceptionistResponse followeesOf(String userId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReceptionistResponse addPublication(String userId, JsonObject messageBodyAsJson) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReceptionistResponse timelineOf(String userId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReceptionistResponse wallOf(String userId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReceptionistResponse likePublicationIdentifiedAs(String publicationId, JsonObject likerAsJson) {
        throw new UnsupportedOperationException();
    }
}
