package bsas.org.openchat;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.time.LocalDateTime;

class PersistedReceptionistLoader {
    private Reader reader;

    public PersistedReceptionistLoader(Reader reader) {
        this.reader = reader;
    }

    public RestReceptionist invoke() throws IOException {
        LineNumberReader lineReader = new LineNumberReader(reader);
        final String[] lastId = new String[1];
        LocalDateTime[] lastNow = new LocalDateTime[1];
        RestReceptionist receptionist = new RestReceptionist(
                new OpenChatSystem(() -> lastNow[0]),
                () -> lastId[0]);

        String line = lineReader.readLine();
        while (line != null) {
            final JsonObject actionAsJson;
            final JsonObject parameters;
            final JsonObject returned;
            try {
                actionAsJson = Json.parse(line).asObject();
                parameters = actionAsJson.get(ActionPersistentReceptionist.PARAMETERS_KEY).asObject();
                returned = actionAsJson.get(ActionPersistentReceptionist.RETURN_KEY).asObject();
            } catch (RuntimeException e) {
                throw new RuntimeException(ActionPersistentReceptionist.invalidRecordErrorMessage(lineReader.getLineNumber()), e);
            }
            final String actionName = actionAsJson.getString(ActionPersistentReceptionist.ACTION_NAME_KEY, "");
            if (actionName.equals(ActionPersistentReceptionist.REGISTER_USER_ACTION_NAME)) {
                lastId[0] = returned.getString(RestReceptionist.ID_KEY, null);
                receptionist.registerUser(parameters);
            } else if (actionName.equals(ActionPersistentReceptionist.FOLLOWINGS_ACTION_NAME)) {
                receptionist.followings(parameters);
            } else if (actionName.equals(ActionPersistentReceptionist.ADD_PUBLICATION_ACTION_NAME)) {
                lastId[0] = returned.getString(RestReceptionist.POST_ID_KEY, null);
                lastNow[0] = LocalDateTime.from(RestReceptionist.DATE_TIME_FORMATTER.parse(
                        returned.getString(RestReceptionist.DATE_TIME_KEY, null)));
                receptionist.addPublication(
                        parameters.getString(RestReceptionist.USER_ID_KEY, null),
                        parameters);
            } else if (actionName.equals(ActionPersistentReceptionist.LIKE_PUBLICATION_ACTION_NAME)) {
                receptionist.likePublicationIdentifiedAs(
                        parameters.getString(RestReceptionist.POST_ID_KEY, null),
                        parameters);
            } else
                throw new RuntimeException(ActionPersistentReceptionist.invalidRecordErrorMessage(lineReader.getLineNumber()));

            line = lineReader.readLine();
        }

        return receptionist;
    }
}
