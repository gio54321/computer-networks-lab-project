package winsome.lib.http;

public enum HTTPResponseCode {
    // only a subset useful to winsome
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NO_CONTENT(204, "No Content"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    NOT_FOUND(404, "Not Found"),
    IM_A_TEAPOT(418, "I'm a teapot"), // see RFC 2324
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    private int code;

    private String phrase;

    private HTTPResponseCode(int code, String phrase) {
        this.code = code;
        this.phrase = phrase;
    }

    public int getCode() {
        return code;
    }

    public static HTTPResponseCode parseFromString(String code) {
        var codeNum = Integer.parseInt(code);
        switch (codeNum) {
            case 200:
                return OK;
            case 201:
                return CREATED;
            case 202:
                return ACCEPTED;
            case 204:
                return NO_CONTENT;
            case 400:
                return BAD_REQUEST;
            case 401:
                return UNAUTHORIZED;
            case 404:
                return NOT_FOUND;
            case 500:
                return INTERNAL_SERVER_ERROR;
            default:
                return null;
        }
    }

    public String getCodeAndReason() {
        return Integer.toString(this.code) + " " + this.phrase;
    }

}
