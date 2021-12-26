package winsome.lib.rest;

public class RESTResponse {
    private int responseCode;
    // TODO object?
    private Object body;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
