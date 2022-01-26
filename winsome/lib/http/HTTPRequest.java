package winsome.lib.http;

/**
 * Class that represent a HTTP request.
 */
public class HTTPRequest extends HTTPMessage {
    // the method of the request
    private HTTPMethod method;
    // path of the request
    private String path;

    public HTTPRequest() {
    }

    public HTTPRequest(HTTPMethod method, String path) {
        this.method = method;
        this.path = path;
    }

    /**
     * Get the formatted start line
     * 
     * @return the formatted start line
     */
    public String getFormattedStartLine() {
        return this.method.getMethodString() + " " + this.path + " " + this.HTTPVersion;
    }

    /**
     * Parse the start line. In particular it parses the method, the path and the
     * HTTP version
     * 
     * @param line the line to be parsed
     */
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

    /**
     * Get the request method
     * 
     * @return the request method
     */
    public HTTPMethod getMethod() {
        return method;
    }

    /**
     * Set the request method
     * 
     * @param method
     */
    public void setMethod(HTTPMethod method) {
        this.method = method;
    }

    /**
     * Get the request path
     * 
     * @return the request path
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the request path
     * 
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Set the request body. This affects the Content-length header
     * that is set equal to the length of the new body
     * 
     * @param body the new body
     * @return the modfied HTTP request changed
     */
    public HTTPRequest setBody(String body) {
        super.setBodySuper(body);
        return this;
    }

    /**
     * Set the header with the new value. If the value is null then
     * the header entry is removed
     * 
     * @param key
     * @param value
     * @return the modified HTTP request
     */
    public HTTPRequest setHeader(String key, String value) {
        super.setHeaderSuper(key, value);
        return this;
    }

}
