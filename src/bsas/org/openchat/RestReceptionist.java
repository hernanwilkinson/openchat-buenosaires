package bsas.org.openchat;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import spark.Request;
import spark.Response;

import java.util.UUID;

import static org.eclipse.jetty.http.HttpStatus.CREATED_201;

public class RestReceptionist {
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String ABOUT_KEY = "about";
    public static final String ID_KEY = "id";

    public ReceptionistResponse registerUser(String registrationBody) {
        JsonObject registrationAsJson = Json.parse(registrationBody).asObject();

        registrationAsJson.add(ID_KEY, UUID.randomUUID().toString());
        registrationAsJson.remove(PASSWORD_KEY);
        return new ReceptionistResponse(CREATED_201,registrationAsJson.toString());

    }
}
