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
        createRegisterUserAction();
        createFollowingsAction();
        createAddPublicationAction();
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

    public void createRegisterUserAction() throws NoSuchMethodException {
        persistentActions.put(
            Receptionist.class.getMethod("registerUser", JsonObject.class),
            new PersistentAction(
                REGISTER_USER_ACTION_NAME,
                response -> response.responseBodyAsJson()));
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

    public void createFollowingsAction() throws NoSuchMethodException {
        persistentActions.put(
                Receptionist.class.getMethod("followings", JsonObject.class),
                new PersistentAction(
                    FOLLOWINGS_ACTION_NAME,
                    response -> new JsonObject()));
    }

    @Override
    public ReceptionistResponse followeesOf(String userId) {
        return receptionist.followeesOf(userId);
    }

    @Override
    public ReceptionistResponse addPublication(String userId, JsonObject messageBodyAsJson) {
        try {
            return (ReceptionistResponse) invoke(this,
                    Receptionist.class.getMethod("addPublication", String.class, JsonObject.class),
                    new Object[]{userId,messageBodyAsJson});
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public void createAddPublicationAction() throws NoSuchMethodException {
        persistentActions.put(
                Receptionist.class.getMethod("addPublication", String.class, JsonObject.class),
                new PersistentAction(
                        ADD_PUBLICATION_ACTION_NAME,
                        response -> response.responseBodyAsJson(),
                        args->addPublicationParameters(
                                (String) args[0],
                                (JsonObject) args[1])));
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
        return new PersistentAction(
                LIKE_PUBLICATION_ACTION_NAME,
                response -> response.responseBodyAsJson(),
                args->likeParameters(
                        (String) args[0],
                        (JsonObject) args[1])).persistAction(
                receptionist.likePublicationIdentifiedAs(publicationId,likerAsJson),
                new Object[]{publicationId, likerAsJson},
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
                args,
                this);
    }
}
