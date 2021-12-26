package winsome.lib.rest;

public class RESTRequest {
    private RESTMethod method;
    private String path;
    // TODO Object?
    private Object body;

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

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
