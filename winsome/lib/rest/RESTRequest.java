package winsome.lib.rest;

public class RESTRequest {
    private RESTMethod method;
    private String path;
    private String body;

    public RESTMethod getMethod() {
        return method;
    }

    public void setMethod(RESTMethod method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
