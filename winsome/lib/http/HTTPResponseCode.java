package winsome.lib.http;

/**
 * Enumeration class containing a subset of HTTP response codes
 */
public enum HTTPResponseCode {
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NO_CONTENT(204, "No Content"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    NOT_FOUND(404, "Not Found"),
    // IM_A_TEAPOT(418, "I'm a teapot"), // see RFC 2324
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    private int code;

    private String phrase;

    private HTTPResponseCode(int code, String phrase) {
        this.code = code;
        this.phrase = phrase;
    }

    /**
     * Get the integer code
     * 
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * Parse a response code
     * 
     * @param code the code string
     * @return null if the code represent an invalid or usupported response code, a
     *         new HTTPResponseCode otherwise
     */
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
            case 422:
                return UNPROCESSABLE_ENTITY;
            case 500:
                return INTERNAL_SERVER_ERROR;
            default:
                return null;
        }
    }

    /**
     * Get formatted code and reason string
     * 
     * @return code and reason string
     */
    public String getCodeAndReason() {
        return Integer.toString(this.code) + " " + this.phrase;
    }

}
