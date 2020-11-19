package bsas.org.openchat;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.eclipse.jetty.http.HttpStatus;

public class ReceptionistResponse {
    private final int status;
    private final String responseBody;

    public ReceptionistResponse(int status, String responseBody) {
        this.status = status;
        this.responseBody = responseBody;
    }

    /* Lamentablemente no se puede usar solo un Json como responseBody
     * porque hay API que devuelven un String que no es un Json string
     * como cuando se produce un error o followings, etc - Hernan
     */
    public ReceptionistResponse(int status, JsonValue bodyAsJson) {
        this(status,bodyAsJson.toString());
    }

    public boolean isStatus(int potentialStatus) {
        return status()==potentialStatus;
    }

    public String responseBody() {
        return responseBody;
    }

    public int status() {
        return status;
    }

    public JsonObject responseBodyAsJson() {
        return Json.parse(responseBody).asObject();
    }

    public JsonArray responseBodyAsJsonArray() {
        return Json.parse(responseBody).asArray();
    }

    String idFromBody() {
        return responseBodyAsJson().getString(RestReceptionist.ID_KEY, null);
    }

    public boolean isSucessStatus() {
        return status>= HttpStatus.OK_200 && status <HttpStatus.MULTIPLE_CHOICES_300;
    }
}
