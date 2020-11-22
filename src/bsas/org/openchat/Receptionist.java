package bsas.org.openchat;

import com.eclipsesource.json.JsonObject;

public interface Receptionist {
    ReceptionistResponse registerUser(JsonObject registrationBodyAsJson);

    ReceptionistResponse login(JsonObject loginBodyAsJson);

    ReceptionistResponse users();

    ReceptionistResponse followings(JsonObject followingsBodyAsJson);

    ReceptionistResponse followersOf(String userId);

    ReceptionistResponse addPublication(String userId, JsonObject messageBodyAsJson);

    ReceptionistResponse timelineOf(String userId);

    ReceptionistResponse wallOf(String userId);

    ReceptionistResponse likePublicationIdentifiedAs(String publicationId, JsonObject likerAsJson);
}
