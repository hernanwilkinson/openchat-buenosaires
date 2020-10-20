package bsas.org.openchat;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.eclipse.jetty.http.HttpStatus.*;

public class RestReceptionist {
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String ABOUT_KEY = "about";
    public static final String ID_KEY = "id";
    public static final String INVALID_CREDENTIALS = "Invalid credentials.";
    private final OpenChatSystem system;
    private final Map<User,String> idsByUser = new HashMap<>();

    public RestReceptionist(OpenChatSystem system) {
        this.system = system;
    }

    public ReceptionistResponse registerUser(String registrationBody) {
        JsonObject registrationAsJson = Json.parse(registrationBody).asObject();

        try {
            User registeredUser = system.register(
                    userNameFrom(registrationAsJson),
                    passwordFrom(registrationAsJson),
                    registrationAsJson.getString(ABOUT_KEY, ""));

            final String registeredUserId = UUID.randomUUID().toString();
            idsByUser.put(registeredUser,registeredUserId);

            JsonObject responseAsJson = userResponseAsJson(registeredUser, registeredUserId);

            return new ReceptionistResponse(CREATED_201, responseAsJson.toString());
        } catch (RuntimeException error){
            return new ReceptionistResponse(BAD_REQUEST_400,error.getMessage());
        }
    }

    public ReceptionistResponse login(String loginBody) {
        JsonObject loginBodyAsJson = Json.parse(loginBody).asObject();

        return system.withAuthenticatedUserDo(
                userNameFrom(loginBodyAsJson),
                passwordFrom(loginBodyAsJson),
            authenticatedUser->authenticatedUserResponse(authenticatedUser),
            ()-> {return new ReceptionistResponse(NOT_FOUND_404,INVALID_CREDENTIALS);});
    }

    private String passwordFrom(JsonObject registrationAsJson) {
        return registrationAsJson.getString(PASSWORD_KEY, "");
    }

    private String userNameFrom(JsonObject registrationAsJson) {
        return registrationAsJson.getString(USERNAME_KEY, "");
    }

    private JsonObject userResponseAsJson(User registeredUser, String registeredUserId) {
        return new JsonObject()
                .add(ID_KEY, registeredUserId)
                .add(USERNAME_KEY, registeredUser.name())
                .add(ABOUT_KEY, registeredUser.about());
    }

    private ReceptionistResponse authenticatedUserResponse(User authenticatedUser) {
        String id = idsByUser.get(authenticatedUser);

        JsonObject responseAsJson = userResponseAsJson(authenticatedUser, id);
        return new ReceptionistResponse(OK_200,responseAsJson.toString());
    }

    public ReceptionistResponse users() {
        JsonArray usersAsJsonArray = new JsonArray();

        system.users().stream()
                .map(user->userResponseAsJson(user,idsByUser.get(user)))
                .forEach(userAsJson->usersAsJsonArray.add(userAsJson));

        return new ReceptionistResponse(OK_200,usersAsJsonArray.toString());
    }
}
