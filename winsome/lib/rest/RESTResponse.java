package winsome.lib.rest;

import winsome.lib.rest.router.ResponseCode;

public class RESTResponse {
    private ResponseCode responseCode;
    private String body;

    public RESTResponse(ResponseCode responseCode, String body) {
        this.responseCode = responseCode;
        this.body = body;
    }

    public RESTResponse(ResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(ResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
