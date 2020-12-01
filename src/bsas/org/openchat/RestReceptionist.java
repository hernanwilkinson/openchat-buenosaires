package bsas.org.openchat;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import org.hibernate.exception.ConstraintViolationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

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

    private final Supplier<OpenChatSystem> systemFactory;

    public RestReceptionist(Supplier<OpenChatSystem> systemFactory) {
        this.systemFactory = systemFactory;
    }

    public ReceptionistResponse registerUser(JsonObject registrationBodyAsJson) {
        return retryWhileCommitConflict(system -> {
            try {
                User registeredUser = system.register(
                        userNameFrom(registrationBodyAsJson),
                        passwordFrom(registrationBodyAsJson),
                        aboutFrom(registrationBodyAsJson),
                        homePageFrom(registrationBodyAsJson));

                return new ReceptionistResponse(
                        CREATED_201,
                        userResponseAsJson(registeredUser));
            } catch (ModelException error) {
                return new ReceptionistResponse(BAD_REQUEST_400, error.getMessage());
            }
        });
    }

    private ReceptionistResponse retryWhileCommitConflict(Function<OpenChatSystem, ReceptionistResponse> transactionableClosure) {
        return retryWhileCommitConflict(transactionableClosure, 5);
    }

    private ReceptionistResponse retryWhileCommitConflict(Function<OpenChatSystem, ReceptionistResponse> transactionableClosure, int retriesLimit) {
        int retries = 0;

        while (retries < retriesLimit) {
            try {
                return executeInTransaction(transactionableClosure);
            } catch (ConstraintViolationException exception) {
                retries++;
            }
        }

        return new ReceptionistResponse(CONFLICT_409,"Commit conflict");
    }

    private ReceptionistResponse executeInTransaction(Function<OpenChatSystem, ReceptionistResponse> transactionableClosure) {
        OpenChatSystem system = null;
        try {
            system = this.systemFactory.get();
            system.start();

            system.beginTransaction();
            final ReceptionistResponse response = transactionableClosure.apply(system);
            // No necesariamente hay que hacer un rollback si falla puesto que
            // por cómo está programada la solución, siempre se verifican las
            // precondiciones antes de hacer algo, pero si llega a existir
            // algún error de programación dejaría la base inconsistente, por
            // eso mejor prevenir que curar y rollbackear - Hernan
            if (response.isSuccessfully())
                system.commitTransaction();
            else
                system.rollbackTransaction();

            system.stop();
            return response;
        } catch (Throwable throwable) {
            system.rollbackTransaction();
            system.stop();
            throw throwable;
        }
    }

    public ReceptionistResponse login(JsonObject loginBodyAsJson) {
        return retryWhileCommitConflict(system-> system.withAuthenticatedUserDo(
                userNameFrom(loginBodyAsJson),
                passwordFrom(loginBodyAsJson),
                authenticatedUser -> authenticatedUserResponse(authenticatedUser),
                () -> new ReceptionistResponse(NOT_FOUND_404, INVALID_CREDENTIALS)));
    }

    public ReceptionistResponse users() {
        return retryWhileCommitConflict(system-> okResponseWithUserArrayFrom(system.users()));
    }

    public ReceptionistResponse followings(JsonObject followingsBodyAsJson) {
        return retryWhileCommitConflict(system-> {
            String followedId = followingsBodyAsJson.getString(FOLLOWED_ID_KEY, "");
            String followerId = followingsBodyAsJson.getString(FOLLOWER_ID_KEY, "");

            try {
                system.followedByFollowerIdentifiedAs(followedId, followerId);

                return new ReceptionistResponse(CREATED_201, FOLLOWING_CREATED);
            } catch (ModelException error) {
                return new ReceptionistResponse(BAD_REQUEST_400, error.getMessage());
            }
        });
    }

    public ReceptionistResponse followersOf(String userId) {
        return retryWhileCommitConflict(system-> okResponseWithUserArrayFrom(system.followersOfUserIdentifiedAs(userId)));
    }

    public ReceptionistResponse addPublication(String userId, JsonObject messageBodyAsJson) {
        return retryWhileCommitConflict(system-> {
            try {
                Publication publication = system.publishForUserIdentifiedAs(
                        userId,
                        messageBodyAsJson.getString("text", ""));

                return new ReceptionistResponse(
                        CREATED_201,
                        publicationAsJson(userId, publication));
            } catch (ModelException error) {
                return new ReceptionistResponse(BAD_REQUEST_400, error.getMessage());
            }
        });
    }

    public ReceptionistResponse timelineOf(String userId) {
        return retryWhileCommitConflict(system-> publicationsAsJson(system.timeLineForUserIdentifiedAs(userId)));
    }

    public ReceptionistResponse wallOf(String userId) {
        return retryWhileCommitConflict(system-> publicationsAsJson(system.wallForUserIdentifiedAs(userId)));
    }

    public ReceptionistResponse likePublicationIdentifiedAs(String publicationId, JsonObject likerAsJson) {
        return retryWhileCommitConflict(system-> {
            try {
                final String userId = likerAsJson.getString(USER_ID_KEY, "");
                int likes = system.likePublicationIdentifiedAs(publicationId, userId);

                JsonObject likesAsJsonObject = new JsonObject()
                        .add(LIKES_KEY, likes);
                return new ReceptionistResponse(OK_200, likesAsJsonObject);
            } catch (ModelException error) {
                return new ReceptionistResponse(BAD_REQUEST_400, error.getMessage());
            }
        });
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
                .add(ID_KEY, registeredUser.restId())
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
                .add(POST_ID_KEY, publication.restId())
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
                        publication.publisherRelatedUser().restId(),
                        publication))
                .forEach(userAsJson -> publicationsAsJsonObject.add(userAsJson));

        return new ReceptionistResponse(OK_200, publicationsAsJsonObject);
    }

}
