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
        this.body = body;
        this.setHeader("Content-Length", Integer.toString(body.length()));
        return this;
    }

    public HTTPRequest setHeader(String key, String value) {
        if (!this.headers.containsKey(key)) {
            this.headers.put(key, value);
        } else {
            this.headers.replace(key, value);
        }
        return this;
    }

}
