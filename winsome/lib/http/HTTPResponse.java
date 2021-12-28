package winsome.lib.http;

import winsome.lib.router.ResponseCode;

public class HTTPResponse {
    private ResponseCode responseCode;
    private String body;

    public HTTPResponse(ResponseCode responseCode, String body) {
        this.responseCode = responseCode;
        this.body = body;
    }

    public HTTPResponse(ResponseCode responseCode) {
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
