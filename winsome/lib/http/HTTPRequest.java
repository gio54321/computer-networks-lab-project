package winsome.lib.http;

public class HTTPRequest extends HTTPMessage {
    private HTTPMethod method;
    private String path;

    public String getFormattedStartLine() {
        return this.method.getMethodString() + " " + this.path + " " + this.HTTPVersion;
    }

    public void parseStartLine(String line) throws HTTPParsingException {
        // parse the request line
        var tokens = line.split(" ");
        if (tokens.length != 3) {
            throw new HTTPParsingException();
        }

        this.method = HTTPMethod.parseFromString(tokens[0]);
        this.path = tokens[1];
        this.HTTPVersion = tokens[2];
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public void setMethod(HTTPMethod method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HTTPRequest setBody(String body) {
        super.setBodySuper(body);
        return this;
    }

    public HTTPRequest setHeader(String key, String value) {
        super.setHeaderSuper(key, value);
        return this;
    }

}
