package bsas.org.openchat;

import com.eclipsesource.json.JsonObject;

public class ReceptionistResponse {
    private final int status;
    private final String responseBody;

    public ReceptionistResponse(int status, String responseBody) {
        this.status = status;
        this.responseBody = responseBody;
    }

    public boolean isStatus(int potentialStatus) {
        return true;
    }

    public String responseBody() {
        return responseBody;
    }
}
