package bsas.org.openchat;

import com.eclipsesource.json.JsonObject;

import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ActionPersistentReceptionist implements Receptionist, InvocationHandler {
    public static final String REGISTER_USER_ACTION_NAME = "registerUser";
    public static final String FOLLOWINGS_ACTION_NAME = "followings";
    public static final String ADD_PUBLICATION_ACTION_NAME = "addPublication";
    public static final String LIKE_PUBLICATION_ACTION_NAME = "likePublication";

    public static final String ACTION_NAME_KEY = "actionName";
    public static final String PARAMETERS_KEY = "parameters";
    public static final String RETURN_KEY = "return";

    private final RestReceptionist receptionist;
    final Writer writer;
    private final List<PersistentAction> persistentActions;

    public ActionPersistentReceptionist(RestReceptionist receptionist, Writer writer) {
        this.receptionist = receptionist;
        this.writer = writer;
        this.persistentActions = new ArrayList<>();
        persistentActions.add(createRegisterUserAction());
    }

    @Override
    public ReceptionistResponse registerUser(JsonObject registrationBodyAsJson) {
        return persistentActions.get(0).persistAction(
                receptionist.registerUser(registrationBodyAsJson),
                registrationBodyAsJson,
                this);
    }

    public PersistentAction createRegisterUserAction() {
        return new PersistentAction(REGISTER_USER_ACTION_NAME, response -> response.responseBodyAsJson());
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
        return new PersistentAction(FOLLOWINGS_ACTION_NAME, response -> new JsonObject()).persistAction(
                receptionist.followings(followingsBodyAsJson),
                followingsBodyAsJson,
                this);
    }

    @Override
    public ReceptionistResponse followeesOf(String userId) {
        return receptionist.followeesOf(userId);
    }

    @Override
    public ReceptionistResponse addPublication(String userId, JsonObject messageBodyAsJson) {
        return new PersistentAction(ADD_PUBLICATION_ACTION_NAME, response -> response.responseBodyAsJson()).persistAction(
                receptionist.addPublication(userId,messageBodyAsJson),
                addPublicationParameters(userId, messageBodyAsJson),
                this);
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
        return new PersistentAction(LIKE_PUBLICATION_ACTION_NAME, response -> response.responseBodyAsJson()).persistAction(
                receptionist.likePublicationIdentifiedAs(publicationId,likerAsJson),
                likeParameters(publicationId, likerAsJson),
                this);
    }

    public JsonObject likeParameters(String publicationId, JsonObject likerAsJson) {
        return new JsonObject(likerAsJson).add(RestReceptionist.POST_ID_KEY,publicationId);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        throw new UnsupportedOperationException();
    }
}
