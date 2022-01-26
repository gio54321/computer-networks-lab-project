package winsome.lib.http;

/**
 * Enumeration class containing a subset of the HTTP request methods
 */
public enum HTTPMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    private String methodString;

    private HTTPMethod(String method) {
        this.methodString = method;
    }

    /**
     * Return a parsed HTTP method
     * 
     * @param method the method string
     * @return null if the method do not correspond to any of the supported HTTP
     *         methods, a new HTTPMethod otherwise
     */
    public static HTTPMethod parseFromString(String method) {
        switch (method) {
            case "GET":
                return GET;
            case "POST":
                return POST;
            case "PUT":
                return PUT;
            case "DELETE":
                return DELETE;
            default:
                return null;
        }
    }

    /**
     * Get method name as String
     * 
     * @return the method String
     */
    public String getMethodString() {
        return this.methodString;
    }
}
