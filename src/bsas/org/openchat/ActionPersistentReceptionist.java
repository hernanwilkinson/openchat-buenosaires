package bsas.org.openchat;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.function.Function;

public class ActionPersistentReceptionist implements Receptionist{
    public static final String REGISTER_USER_ACTION_NAME = "registerUser";
    public static final String FOLLOWINGS_ACTION_NAME = "followings";
    public static final String ADD_PUBLICATION_ACTION_NAME = "addPublication";
    public static final String LIKE_PUBLICATION_ACTION_NAME = "likePublication";

    public static final String ACTION_NAME_KEY = "actionName";
    public static final String PARAMETERS_KEY = "parameters";
    public static final String RETURN_KEY = "return";

    private final RestReceptionist receptionist;
    private final StringWriter writer;

    public ActionPersistentReceptionist(RestReceptionist receptionist, StringWriter writer) {
        this.receptionist = receptionist;
        this.writer = writer;
    }

    public static RestReceptionist recoverFrom(Reader reader) throws IOException {
        LineNumberReader lineReader = new LineNumberReader(reader);
        final String[] lastId = new String[1];
        RestReceptionist receptionist = new RestReceptionist(
                new OpenChatSystem(()-> LocalDateTime.now()),
                ()->lastId[0]);

        String line = lineReader.readLine();
        final JsonObject actionAsJson = Json.parse(line).asObject();
        lastId[0] = actionAsJson.get(RETURN_KEY).asObject().getString(RestReceptionist.ID_KEY,null);
        receptionist.registerUser(actionAsJson.get(PARAMETERS_KEY).asObject());

        return receptionist;
    }

    @Override
    public ReceptionistResponse registerUser(JsonObject registrationBodyAsJson) {
        return persistAction(
                receptionist.registerUser(registrationBodyAsJson),
                REGISTER_USER_ACTION_NAME,
                registrationBodyAsJson);
    }

    @Override
    public ReceptionistResponse login(JsonObject loginBodyAsJson) {
        return receptionist.login(loginBodyAsJson);
    }

    @Override
    public ReceptionistResponse users() {
        return receptionist.users();
    }

    @Override
    public ReceptionistResponse followings(JsonObject followingsBodyAsJson) {
        return persistAction(
                receptionist.followings(followingsBodyAsJson),
                FOLLOWINGS_ACTION_NAME,
                followingsBodyAsJson,
                response-> new JsonObject());
    }

    @Override
    public ReceptionistResponse followeesOf(String userId) {
        return receptionist.followeesOf(userId);
    }

    @Override
    public ReceptionistResponse addPublication(String userId, JsonObject messageBodyAsJson) {
        return persistAction(
                receptionist.addPublication(userId,messageBodyAsJson),
                ADD_PUBLICATION_ACTION_NAME,
                addPublicationParameters(userId, messageBodyAsJson));
    }

    public JsonObject addPublicationParameters(String userId, JsonObject messageBodyAsJson) {
        return new JsonObject(messageBodyAsJson).add(RestReceptionist.USER_ID_KEY,userId);
    }

    @Override
    public ReceptionistResponse timelineOf(String userId) {
        return receptionist.timelineOf(userId);
    }

    @Override
    public ReceptionistResponse wallOf(String userId) {
        return receptionist.wallOf(userId);
    }

    @Override
    public ReceptionistResponse likePublicationIdentifiedAs(String publicationId, JsonObject likerAsJson) {
        return persistAction(
                receptionist.likePublicationIdentifiedAs(publicationId,likerAsJson),
                LIKE_PUBLICATION_ACTION_NAME,
                likeParameters(publicationId, likerAsJson));
    }

    public JsonObject likeParameters(String publicationId, JsonObject likerAsJson) {
        return new JsonObject(likerAsJson).add(RestReceptionist.POST_ID_KEY,publicationId);
    }

    private ReceptionistResponse persistAction(ReceptionistResponse originalResponse, String actionName, JsonObject parameters) {
        return persistAction(
                originalResponse,
                actionName,
                parameters,
                response->response.responseBodyAsJson());
    }

    private ReceptionistResponse persistAction(ReceptionistResponse response,
                                               String actionName, JsonObject parameters, Function<ReceptionistResponse, JsonObject> returnClosure) {

        if(response.isSucessStatus()) {
            JsonObject actionAsJson = new JsonObject()
                    .add(ACTION_NAME_KEY, actionName)
                    .add(PARAMETERS_KEY, parameters)
                    .add(RETURN_KEY, returnClosure.apply(response));

            writer.write(actionAsJson.toString());
            writer.write("\n");
        }
        return response;
    }
}
