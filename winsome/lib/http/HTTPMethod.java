package winsome.lib.http;

public enum HTTPMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    private String methodString;

    private HTTPMethod(String method) {
        this.methodString = method;
    }

    // TODO doc
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

    public String getMethodString() {
        return this.methodString;
    }
}
