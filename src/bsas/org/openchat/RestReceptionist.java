package bsas.org.openchat;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.eclipse.jetty.http.HttpStatus.*;

public class RestReceptionist {
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String ABOUT_KEY = "about";
    public static final String ID_KEY = "id";
    public static final String INVALID_CREDENTIALS = "Invalid credentials.";
    public static final String FOLLOWING_CREATED = "Following created.";
    public static final String FOLLOWER_ID = "followerId";
    public static final String FOLLOWEE_ID = "followeeId";
    public static final String POST_ID_KEY = "postId";
    public static final String USER_ID_KEY = "userId";
    public static final String TEXT_KEY = "text";
    public static final String DATE_TIME_KEY = "dateTime";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final OpenChatSystem system;
    private final Map<User,String> idsByUser = new HashMap<>();
    private final Map<Publication,String> idsByPublication = new HashMap<>();

    public RestReceptionist(OpenChatSystem system) {
        this.system = system;
    }

    public ReceptionistResponse registerUser(JsonObject registrationBodyAsJson) {
        try {
            User registeredUser = system.register(
                    userNameFrom(registrationBodyAsJson),
                    passwordFrom(registrationBodyAsJson),
                    aboutFrom(registrationBodyAsJson));

            final String registeredUserId = UUID.randomUUID().toString();
            idsByUser.put(registeredUser,registeredUserId);

            JsonObject responseAsJson = userResponseAsJson(registeredUser, registeredUserId);

            return new ReceptionistResponse(CREATED_201, responseAsJson.toString());
        } catch (RuntimeException error){
            return new ReceptionistResponse(BAD_REQUEST_400,error.getMessage());
        }
    }

    public ReceptionistResponse login(JsonObject loginBodyAsJson) {
        return system.withAuthenticatedUserDo(
                userNameFrom(loginBodyAsJson),
                passwordFrom(loginBodyAsJson),
            authenticatedUser->authenticatedUserResponse(authenticatedUser),
            ()-> new ReceptionistResponse(NOT_FOUND_404,INVALID_CREDENTIALS));
    }

    public ReceptionistResponse users() {
        return okResponseWithUserArrayFrom(system.users());
    }

    public ReceptionistResponse followings(JsonObject followingsBodyAsJson) {

        String followerId = followingsBodyAsJson.getString(FOLLOWER_ID,"");
        String followeeId = followingsBodyAsJson.getString(FOLLOWEE_ID,"");

        try {
            system.followForUserNamed(
                    userNameIdentifiedAs(followerId),
                    userNameIdentifiedAs(followeeId));

            return new ReceptionistResponse(CREATED_201, FOLLOWING_CREATED);
        } catch (RuntimeException error){
            return new ReceptionistResponse(BAD_REQUEST_400,error.getMessage());
        }
    }

    public ReceptionistResponse followeesOf(String userId) {
        final List<User> followees =
                system.followeesOfUserNamed(userNameIdentifiedAs(userId));

        return okResponseWithUserArrayFrom(followees);
    }

    public ReceptionistResponse addPublication(String userId, String messageBody) {
        JsonObject messageBodyAsJson = Json.parse(messageBody).asObject();

        try {
            Publication publication = system.publishForUserNamed(userNameIdentifiedAs(userId), messageBodyAsJson.getString("text", ""));
            String publicationId = UUID.randomUUID().toString();
            idsByPublication.put(publication, publicationId);

            JsonObject pubicationAsJson = publicationAsJson(userId, publication, publicationId);

            return new ReceptionistResponse(CREATED_201, pubicationAsJson.toString());
        } catch (RuntimeException error){
            return new ReceptionistResponse(BAD_REQUEST_400,error.getMessage());
        }
    }

    private JsonObject publicationAsJson(String userId, Publication publication, String publicationId) {
        return new JsonObject()
                .add(POST_ID_KEY, publicationId)
                .add(USER_ID_KEY, userId)
                .add(TEXT_KEY, publication.message())
                .add(DATE_TIME_KEY, formatDateTime(publication.publicationTime()));
    }

    private String formatDateTime(LocalDateTime dateTimeToFormat) {
        return DATE_TIME_FORMATTER.format(dateTimeToFormat);
    }

    public ReceptionistResponse timelineOf(String userId) {
        List<Publication> timeLine = system.timeLineForUserNamed(userNameIdentifiedAs(userId));
        return publicationsAsJson(timeLine);
    }

    private ReceptionistResponse publicationsAsJson(List<Publication> timeLine) {
        JsonArray publicationsAsJsonObject = new JsonArray();
        timeLine.stream()
                .map(publication -> publicationAsJson(idsByUser.get(publication.publisherRelatedUser()), publication, idsByPublication.get(publication)))
                .forEach(userAsJson -> publicationsAsJsonObject.add(userAsJson));

        return new ReceptionistResponse(OK_200, publicationsAsJsonObject.toString());
    }

    public ReceptionistResponse wallOf(String userId) {
        List<Publication> wall = system.wallForUserNamed(userNameIdentifiedAs(userId));
        return publicationsAsJson(wall);
    }

    private String passwordFrom(JsonObject registrationAsJson) {
        return registrationAsJson.getString(PASSWORD_KEY, "");
    }

    private String userNameFrom(JsonObject registrationAsJson) {
        return registrationAsJson.getString(USERNAME_KEY, "");
    }

    private String aboutFrom(JsonObject registrationAsJson) {
        return registrationAsJson.getString(ABOUT_KEY, "");
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

    private User userIdentifiedAs(String userId) {
        return idsByUser.entrySet().stream()
                .filter(userAndId->userAndId.getValue().equals(userId))
                .findFirst()
                .get().getKey();
    }

    private ReceptionistResponse okResponseWithUserArrayFrom(List<User> users) {
        JsonArray usersAsJsonArray = new JsonArray();
        users.stream()
                .map(user -> userResponseAsJson(user, idsByUser.get(user)))
                .forEach(userAsJson -> usersAsJsonArray.add(userAsJson));

        return new ReceptionistResponse(OK_200, usersAsJsonArray.toString());
    }

    private String userNameIdentifiedAs(String userId) {
        return userIdentifiedAs(userId).name();
    }
}
