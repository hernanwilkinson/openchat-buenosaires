package bsas.org.openchat;

import com.eclipsesource.json.JsonObject;

import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;

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
    private final HashMap<Method,PersistentAction> persistentActions;

    public ActionPersistentReceptionist(RestReceptionist receptionist, Writer writer) throws NoSuchMethodException {
        this.receptionist = receptionist;
        this.writer = writer;
        this.persistentActions = new HashMap<>();
        persistentActions.put(Receptionist.class.getMethod("registerUser", JsonObject.class),createRegisterUserAction());
        persistentActions.put(Receptionist.class.getMethod("followings", JsonObject.class),createFollowingsAction());
    }

    @Override
    public ReceptionistResponse registerUser(JsonObject registrationBodyAsJson) {
        try {
            return (ReceptionistResponse) invoke(this,
                    Receptionist.class.getMethod("registerUser", JsonObject.class),
                    new JsonObject[]{registrationBodyAsJson});
        } catch (Throwable throwable) {
            return null;
        }
    }

    public PersistentAction createRegisterUserAction() throws NoSuchMethodException {
        return new PersistentAction(
                REGISTER_USER_ACTION_NAME,
                response -> response.responseBodyAsJson(),
                Receptionist.class.getMethod("registerUser", JsonObject.class));
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
        try {
            return (ReceptionistResponse) invoke(this,
                    Receptionist.class.getMethod("followings", JsonObject.class),
                    new JsonObject[]{followingsBodyAsJson});
        } catch (Throwable throwable) {
            return null;
        }
    }

    public PersistentAction createFollowingsAction() throws NoSuchMethodException {
        return new PersistentAction(
                FOLLOWINGS_ACTION_NAME,
                response -> new JsonObject(),
                Receptionist.class.getMethod("followings", JsonObject.class));
    }

    @Override
    public ReceptionistResponse followeesOf(String userId) {
        return receptionist.followeesOf(userId);
    }

    @Override
    public ReceptionistResponse addPublication(String userId, JsonObject messageBodyAsJson) {
        return new PersistentAction(ADD_PUBLICATION_ACTION_NAME, response -> response.responseBodyAsJson(), null).persistAction(
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
        return new PersistentAction(LIKE_PUBLICATION_ACTION_NAME, response -> response.responseBodyAsJson(), null).persistAction(
                receptionist.likePublicationIdentifiedAs(publicationId,likerAsJson),
                likeParameters(publicationId, likerAsJson),
                this);
    }

    public JsonObject likeParameters(String publicationId, JsonObject likerAsJson) {
        return new JsonObject(likerAsJson).add(RestReceptionist.POST_ID_KEY,publicationId);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final PersistentAction persistentAction = persistentActions.get(method);

        return persistentAction.persistAction(
                (ReceptionistResponse) method.invoke(receptionist,args),
                (JsonObject) args[0],
                this);
    }
}
