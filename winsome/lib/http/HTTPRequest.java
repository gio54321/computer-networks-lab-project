package winsome.lib.http;

import java.util.HashMap;
import java.util.Map;

public class HTTPRequest extends HTTPMessage {
    private HTTPMethod method;
    private String path;
    private String HTTPVersion = "HTTP/1.1";
    private Map<String, String> headers = new HashMap<>();
    private String body;

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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFormattedMessage() {
        var outStr = this.method.getMethodString() + " " + this.path + " " + this.HTTPVersion + "\r\n";
        for (var k : this.headers.keySet()) {
            outStr += k + ": " + this.headers.get(k);
        }

        return outStr;
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

    public void parseHeaders(String[] headerLines) throws HTTPParsingException {
        // From RFC 7230 section 3.2
        // Each header field consists of a case-insensitive field name followed
        // by a colon (":"), optional leading whitespace, the field value, and
        // optional trailing whitespace.

        for (var headerLine : headerLines) {
            var tokens = headerLine.split(":", 2);
            if (tokens.length != 2) {
                throw new HTTPParsingException();
            }
            this.headers.put(tokens[0], tokens[1].trim());
        }
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

}
