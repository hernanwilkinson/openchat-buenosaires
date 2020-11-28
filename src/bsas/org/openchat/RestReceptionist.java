package bsas.org.openchat;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.eclipse.jetty.http.HttpStatus.*;

public class RestReceptionist {
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String ABOUT_KEY = "about";
    public static final String HOME_PAGE_KEY = "homePage";
    public static final String ID_KEY = "id";
    public static final String FOLLOWED_ID_KEY = "followerId";
    public static final String FOLLOWER_ID_KEY = "followeeId";
    public static final String POST_ID_KEY = "postId";
    public static final String USER_ID_KEY = "userId";
    public static final String TEXT_KEY = "text";
    public static final String DATE_TIME_KEY = "dateTime";
    public static final String LIKES_KEY = "likes";
    public static final String PUBLICATION_ID_KEY = "publicationId";
    public static final String INVALID_CREDENTIALS = "Invalid credentials.";
    public static final String FOLLOWING_CREATED = "Following created.";
    public static final String INVALID_PUBLICATION = "Invalid post";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final OpenChatSystem system;

    public RestReceptionist(OpenChatSystem system) {
        this.system = system;
    }

    public ReceptionistResponse registerUser(JsonObject registrationBodyAsJson) {
        try {
            User registeredUser = system.register(
                    userNameFrom(registrationBodyAsJson),
                    passwordFrom(registrationBodyAsJson),
                    aboutFrom(registrationBodyAsJson),
                    homePageFrom(registrationBodyAsJson));

            return new ReceptionistResponse(
                    CREATED_201,
                    userResponseAsJson(registeredUser));
        } catch (ModelException error){
            return new ReceptionistResponse(BAD_REQUEST_400,error.getMessage());
        }
    }

    public ReceptionistResponse login(JsonObject loginBodyAsJson) {
        return system.withAuthenticatedUserDo(
                userNameFrom(loginBodyAsJson),
                passwordFrom(loginBodyAsJson),
            authenticatedUser->authenticatedUserResponse(authenticatedUser),
            ()-> new ReceptionistResponse(NOT_FOUND_404, INVALID_CREDENTIALS));
    }

    public ReceptionistResponse users() {
        return okResponseWithUserArrayFrom(system.users());
    }

    public ReceptionistResponse followings(JsonObject followingsBodyAsJson) {
        String followedId = followingsBodyAsJson.getString(FOLLOWED_ID_KEY,"");
        String followerId = followingsBodyAsJson.getString(FOLLOWER_ID_KEY,"");

        try {
            system.followedByFollowerIdentifiedAs(followedId, followerId);

            return new ReceptionistResponse(CREATED_201, FOLLOWING_CREATED);
        } catch (ModelException error){
            return new ReceptionistResponse(BAD_REQUEST_400,error.getMessage());
        }
    }

    public ReceptionistResponse followersOf(String userId) {
        final List<User> followers = system.followersOfUserIdentifiedAs(userId);

        return okResponseWithUserArrayFrom(followers);
    }

    public ReceptionistResponse addPublication(String userId, JsonObject messageBodyAsJson) {
        try {
            Publication publication = system.publishForUserIdentifiedAs(
                    userId,
                    messageBodyAsJson.getString("text", ""));

            return new ReceptionistResponse(
                    CREATED_201,
                    publicationAsJson(userId, publication));
        } catch (ModelException error){
            return new ReceptionistResponse(BAD_REQUEST_400,error.getMessage());
        }
    }

    public ReceptionistResponse timelineOf(String userId) {
        List<Publication> timeLine = system.timeLineOfUserIdentifiedAs(userId);

        return publicationsAsJson(timeLine);
    }

    public ReceptionistResponse wallOf(String userId) {
        List<Publication> wall = system.wallOfUserIdentifiedAs(userId);

        return publicationsAsJson(wall);
    }

    public ReceptionistResponse likePublicationIdentifiedAs(String publicationId, JsonObject likerAsJson) {
        try {
            final String likerId = likerAsJson.getString(USER_ID_KEY, "");
            int likes = system.likePublicationIdentifiedAs(publicationId, likerId);

            JsonObject likesAsJsonObject = new JsonObject()
                    .add(LIKES_KEY, likes);
            return new ReceptionistResponse(OK_200, likesAsJsonObject);
        } catch (ModelException error) {
            return new ReceptionistResponse(BAD_REQUEST_400,error.getMessage());
        }
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

    private String homePageFrom(JsonObject registrationAsJson) {
        return registrationAsJson.getString(HOME_PAGE_KEY, "");
    }

    private JsonObject userResponseAsJson(User registeredUser) {
        return new JsonObject()
                .add(ID_KEY, registeredUser.id())
                .add(USERNAME_KEY, registeredUser.name())
                .add(ABOUT_KEY, registeredUser.about())
                .add(HOME_PAGE_KEY,registeredUser.homePage());
    }

    private ReceptionistResponse authenticatedUserResponse(User authenticatedUser) {
        JsonObject responseAsJson = userResponseAsJson(authenticatedUser);

        return new ReceptionistResponse(OK_200,responseAsJson.toString());
    }

    private ReceptionistResponse okResponseWithUserArrayFrom(List<User> users) {
        JsonArray usersAsJsonArray = new JsonArray();

        users.stream()
                .map(user -> userResponseAsJson(user))
                .forEach(userAsJson -> usersAsJsonArray.add(userAsJson));

        return new ReceptionistResponse(OK_200, usersAsJsonArray);
    }

    private JsonObject publicationAsJson(String userId, Publication publication) {
        return new JsonObject()
                .add(POST_ID_KEY, publication.id())
                .add(USER_ID_KEY, userId)
                .add(TEXT_KEY, publication.message())
                .add(DATE_TIME_KEY, formatDateTime(publication.publicationTime()))
                .add(LIKES_KEY, publication.likes());
    }

    private String formatDateTime(LocalDateTime dateTimeToFormat) {
        return DATE_TIME_FORMATTER.format(dateTimeToFormat);
    }

    private ReceptionistResponse publicationsAsJson(List<Publication> timeLine) {
        JsonArray publicationsAsJsonObject = new JsonArray();

        timeLine.stream()
                .map(publication -> publicationAsJson(
                        publication.userId(),
                        publication))
                .forEach(userAsJson -> publicationsAsJsonObject.add(userAsJson));

        return new ReceptionistResponse(OK_200, publicationsAsJsonObject);
    }

}
