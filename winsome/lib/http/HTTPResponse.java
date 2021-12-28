package winsome.lib.http;

public class HTTPResponse {
    private HTTPResponseCode responseCode;
    private String body;

    public HTTPResponse(HTTPResponseCode responseCode, String body) {
        this.responseCode = responseCode;
        this.body = body;
    }

    public HTTPResponse(HTTPResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    public HTTPResponseCode getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(HTTPResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
