package bsas.org.openchat;

import com.eclipsesource.json.JsonObject;

import java.io.StringWriter;
import java.util.function.Function;

public class ActionPersistentReceptionist implements Receptionist{
    public static final String ACTION_NAME_KEY = "actionName";
    public static final String REGISTER_USER_ACTION_NAME = "registerUser";
    public static final String PARAMETERS_KEY = "parameters";
    public static final String RETURN_KEY = "return";
    public static final String FOLLOWINGS_ACTION_NAME = "followings";
    private final RestReceptionist receptionist;
    private final StringWriter writer;

    public ActionPersistentReceptionist(RestReceptionist receptionist, StringWriter writer) {
        this.receptionist = receptionist;
        this.writer = writer;
    }

    @Override
    public ReceptionistResponse registerUser(JsonObject registrationBodyAsJson) {
        return persistAction(
                receptionist.registerUser(registrationBodyAsJson),
                REGISTER_USER_ACTION_NAME,
                registrationBodyAsJson,
                response->response.idFromBody());
    }

    private ReceptionistResponse persistAction(ReceptionistResponse response,
        String actionName, JsonObject parameters, Function<ReceptionistResponse,String> returnedObject) {

        if(response.isSucessStatus()) {
            JsonObject actionAsJson = new JsonObject()
                    .add(ACTION_NAME_KEY, actionName)
                    .add(PARAMETERS_KEY, parameters)
                    .add(RETURN_KEY, returnedObject.apply(response));

            writer.write(actionAsJson.toString());
            writer.write("\n");
        }
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
        return persistAction(
                receptionist.followings(followingsBodyAsJson),
                FOLLOWINGS_ACTION_NAME,
                followingsBodyAsJson,
                response-> "");
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
