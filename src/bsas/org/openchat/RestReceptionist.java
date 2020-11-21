package bsas.org.openchat;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.eclipse.jetty.http.HttpStatus.*;

public class RestReceptionist implements Receptionist {
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String ABOUT_KEY = "about";
    public static final String HOME_PAGE_KEY = "homePage";
    public static final String ID_KEY = "id";
    public static final String FOLLOWER_ID_KEY = "followerId";
    public static final String FOLLOWEE_ID_KEY = "followeeId";
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
    private final Supplier<String> idGenerator;
    private final Map<User,String> idsByUser = new HashMap<>();
    private final Map<Publication,String> idsByPublication = new HashMap<>();

    public RestReceptionist(OpenChatSystem system) {
        this(system, () -> UUID.randomUUID().toString());
    }

    public RestReceptionist(OpenChatSystem system, Supplier<String> idGenerator) {
        this.system = system;
        this.idGenerator = idGenerator;
    }

    @Override
    public ReceptionistResponse registerUser(JsonObject registrationBodyAsJson) {
        try {
            User registeredUser = system.register(
                    userNameFrom(registrationBodyAsJson),
                    passwordFrom(registrationBodyAsJson),
                    aboutFrom(registrationBodyAsJson),
                    homePageFrom(registrationBodyAsJson));

            final String registeredUserId = generateId();
            idsByUser.put(registeredUser,registeredUserId);

            return new ReceptionistResponse(
                    CREATED_201,
                    userResponseAsJson(registeredUser, registeredUserId));
        } catch (ModelException error){
            return new ReceptionistResponse(BAD_REQUEST_400,error.getMessage());
        }
    }

    public String generateId() {
        return idGenerator.get();
    }

    @Override
    public ReceptionistResponse login(JsonObject loginBodyAsJson) {
        return system.withAuthenticatedUserDo(
                userNameFrom(loginBodyAsJson),
                passwordFrom(loginBodyAsJson),
            authenticatedUser->authenticatedUserResponse(authenticatedUser),
            ()-> new ReceptionistResponse(NOT_FOUND_404, INVALID_CREDENTIALS));

    }

    @Override
    public ReceptionistResponse users() {
        return okResponseWithUserArrayFrom(system.users());
    }

    @Override
    public ReceptionistResponse followings(JsonObject followingsBodyAsJson) {

        String followerId = followingsBodyAsJson.getString(FOLLOWER_ID_KEY,"");
        String followeeId = followingsBodyAsJson.getString(FOLLOWEE_ID_KEY,"");

        try {
            system.followForUserNamed(
                    userNameIdentifiedAs(followerId),
                    userNameIdentifiedAs(followeeId));

            return new ReceptionistResponse(CREATED_201, FOLLOWING_CREATED);
        } catch (ModelException error){
            return new ReceptionistResponse(BAD_REQUEST_400,error.getMessage());
        }
    }

    @Override
    public ReceptionistResponse followeesOf(String userId) {
        final List<User> followees =
                system.followeesOfUserNamed(userNameIdentifiedAs(userId));

        return okResponseWithUserArrayFrom(followees);
    }

    @Override
    public ReceptionistResponse addPublication(String userId, JsonObject messageBodyAsJson) {
        try {
            Publication publication = system.publishForUserNamed(userNameIdentifiedAs(userId), messageBodyAsJson.getString("text", ""));
            String publicationId = generateId();
            idsByPublication.put(publication, publicationId);

            return new ReceptionistResponse(
                    CREATED_201,
                    publicationAsJson(userId, publication, publicationId));
        } catch (ModelException error){
            return new ReceptionistResponse(BAD_REQUEST_400,error.getMessage());
        }
    }

    @Override
    public ReceptionistResponse timelineOf(String userId) {
        List<Publication> timeLine =
                system.timeLineForUserNamed(userNameIdentifiedAs(userId));

        return publicationsAsJson(timeLine);
    }

    @Override
    public ReceptionistResponse wallOf(String userId) {
        List<Publication> wall = system.wallForUserNamed(userNameIdentifiedAs(userId));

        return publicationsAsJson(wall);
    }

    @Override
    public ReceptionistResponse likePublicationIdentifiedAs(String publicationId, JsonObject likerAsJson) {
        try {
            final String userName = userNameIdentifiedAs(likerAsJson.getString(USER_ID_KEY, ""));
            final Publication publication = idsByPublication.entrySet().stream()
                    .filter(idByPublication->idByPublication.getValue().equals(publicationId))
                    .findFirst()
                    .map(idByPublication->idByPublication.getKey())
                    .orElseThrow(()->new ModelException(INVALID_PUBLICATION));

            int likes = system.likePublication(publication, userName);

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

    private JsonObject userResponseAsJson(User registeredUser, String registeredUserId) {
        return new JsonObject()
                .add(ID_KEY, registeredUserId)
                .add(USERNAME_KEY, registeredUser.name())
                .add(ABOUT_KEY, registeredUser.about())
                .add(HOME_PAGE_KEY,registeredUser.homePage());
    }

    private ReceptionistResponse authenticatedUserResponse(User authenticatedUser) {
        JsonObject responseAsJson = userResponseAsJson(
                authenticatedUser,
                userIdFor(authenticatedUser));

        return new ReceptionistResponse(OK_200,responseAsJson.toString());
    }

    private User userIdentifiedAs(String userId) {
        return idsByUser.entrySet().stream()
                .filter(userAndId->userAndId.getValue().equals(userId))
                .findFirst()
                .map(userAndId->userAndId.getKey())
                .orElseThrow(()->new ModelException(INVALID_CREDENTIALS));
    }

    private ReceptionistResponse okResponseWithUserArrayFrom(List<User> users) {
        JsonArray usersAsJsonArray = new JsonArray();

        users.stream()
                .map(user -> userResponseAsJson(user, userIdFor(user)))
                .forEach(userAsJson -> usersAsJsonArray.add(userAsJson));

        return new ReceptionistResponse(OK_200, usersAsJsonArray);
    }

    private String userNameIdentifiedAs(String userId) {
        return userIdentifiedAs(userId).name();
    }

    private JsonObject publicationAsJson(String userId, Publication publication, String publicationId) {
        return new JsonObject()
                .add(POST_ID_KEY, publicationId)
                .add(USER_ID_KEY, userId)
                .add(TEXT_KEY, publication.message())
                .add(DATE_TIME_KEY, formatDateTime(publication.publicationTime()))
                .add(LIKES_KEY,system.likesOf(publication));
    }

    private String formatDateTime(LocalDateTime dateTimeToFormat) {
        return DATE_TIME_FORMATTER.format(dateTimeToFormat);
    }

    private ReceptionistResponse publicationsAsJson(List<Publication> timeLine) {
        JsonArray publicationsAsJsonObject = new JsonArray();

        timeLine.stream()
                .map(publication -> publicationAsJson(
                        userIdFor(publication.publisherRelatedUser()),
                        publication,
                        publicationIdFor(publication)))
                .forEach(userAsJson -> publicationsAsJsonObject.add(userAsJson));

        return new ReceptionistResponse(OK_200, publicationsAsJsonObject);
    }

    private String publicationIdFor(Publication publication) {
        return idsByPublication.get(publication);
    }

    private String userIdFor(User user) {
        return idsByUser.get(user);
    }

}
