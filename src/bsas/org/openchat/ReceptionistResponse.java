package bsas.org.openchat;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import spark.Response;

public class ReceptionistResponse {
    private final int status;
    private final String responseBody;

    public ReceptionistResponse(int status, String responseBody) {
        this.status = status;
        this.responseBody = responseBody;
    }

    public boolean isStatus(int potentialStatus) {
        return status==potentialStatus;
    }

    public String responseBody() {
        return responseBody;
    }

    public int status() {
        return status;
    }

    JsonObject responseBodyAsJson() {
        return Json.parse(responseBody).asObject();
    }

    public String toResponseInto(Response response) {
        response.status(status());
        response.type("application/json");

        return responseBody;
    }

    public JsonArray responseBodyAsJsonArray() {
        return Json.parse(responseBody).asArray();
    }
}
