package bsas.org.openchat;

import com.eclipsesource.json.JsonObject;

import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

public class ActionPersistentReceptionist implements InvocationHandler {
    public static final String REGISTER_USER_ACTION_NAME = "registerUser";
    public static final String FOLLOWINGS_ACTION_NAME = "followings";
    public static final String ADD_PUBLICATION_ACTION_NAME = "addPublication";
    public static final String LIKE_PUBLICATION_ACTION_NAME = "likePublication";

    private final RestReceptionist receptionist;
    private HashMap<Method,PersistentAction> persistentActions;

    public ActionPersistentReceptionist(RestReceptionist receptionist, Writer writer) throws NoSuchMethodException {
        this.receptionist = receptionist;
        createPersistentActions(writer);
    }

    public void createPersistentActions(Writer writer) throws NoSuchMethodException {
        this.persistentActions = new HashMap<>();

        createRegisterUserAction(writer);
        createFollowingsAction(writer);
        createAddPublicationAction(writer);
        createLikePublicationAction(writer);
    }

    public static Receptionist asProxyOf(RestReceptionist receptionist, Writer writer) throws NoSuchMethodException {
        return (Receptionist) Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(),
                new Class[]{Receptionist.class},
                new ActionPersistentReceptionist(receptionist,writer));
    }

    public void createRegisterUserAction(Writer writer) throws NoSuchMethodException {
        persistentActions.put(
            Receptionist.class.getMethod("registerUser", JsonObject.class),
            new PersistentAction(
                REGISTER_USER_ACTION_NAME,
                response -> response.responseBodyAsJson(),
                writer));
    }

    public void createFollowingsAction(Writer writer) throws NoSuchMethodException {
        persistentActions.put(
                Receptionist.class.getMethod("followings", JsonObject.class),
                new PersistentAction(
                    FOLLOWINGS_ACTION_NAME,
                    response -> new JsonObject(),
                    writer));
    }

    public void createAddPublicationAction(Writer writer) throws NoSuchMethodException {
        persistentActions.put(
                Receptionist.class.getMethod("addPublication", String.class, JsonObject.class),
                new PersistentAction(
                        ADD_PUBLICATION_ACTION_NAME,
                        response -> response.responseBodyAsJson(),
                        writer,
                        args->addPublicationParameters(
                                (String) args[0],
                                (JsonObject) args[1])));
    }

    public JsonObject addPublicationParameters(String userId, JsonObject messageBodyAsJson) {
        return new JsonObject(messageBodyAsJson).add(RestReceptionist.USER_ID_KEY,userId);
    }

    public void createLikePublicationAction(Writer writer) throws NoSuchMethodException {
        persistentActions.put(
                Receptionist.class.getMethod("likePublicationIdentifiedAs", String.class, JsonObject.class),
                new PersistentAction(
                    LIKE_PUBLICATION_ACTION_NAME,
                    response -> response.responseBodyAsJson(),
                    writer,
                    args -> likeParameters(
                            (String) args[0],
                            (JsonObject) args[1])));
    }

    public JsonObject likeParameters(String publicationId, JsonObject likerAsJson) {
        return new JsonObject(likerAsJson).add(RestReceptionist.POST_ID_KEY,publicationId);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final PersistentAction persistentAction = persistentActions.get(method);

        if(persistentAction==null)
            return (ReceptionistResponse) method.invoke(receptionist,args);
        else
            return persistentAction.persistAction(
                    (ReceptionistResponse) method.invoke(receptionist,args),
                    args,
                    this);
    }
}
