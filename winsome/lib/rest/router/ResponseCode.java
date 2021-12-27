package winsome.lib.rest.router;

public enum ResponseCode {
    // only a subset useful to winsome
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NO_CONTENT(204, "No Content"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    NOT_FOUND(404, "Not Found"),
    IM_A_TEAPOT(418, "I'm a teapot"); // see RFC 2324

    private int code;

    private String phrase;

    private ResponseCode(int code, String phrase) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getFullStatusLine() {
        return Integer.toString(this.code) + " " + this.phrase;
    }

}
