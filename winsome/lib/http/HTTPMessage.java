package winsome.lib.http;

import java.util.HashMap;
import java.util.Map;

public abstract class HTTPMessage {
    protected String HTTPVersion = "HTTP/1.1";
    protected Map<String, String> headers = new HashMap<>();
    protected String body;

    public abstract String getFormattedStartLine();

    public abstract void parseStartLine(String line) throws HTTPParsingException;

    public String getFormattedMessage() {
        var outStr = this.getFormattedStartLine() + "\r\n";

        for (var k : this.headers.keySet()) {
            outStr += k + ": " + this.headers.get(k) + "\r\n";
        }

        outStr += "\r\n";

        if (this.body != null) {
            outStr += this.body;
        }
        return outStr;
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

    public String getBody() {
        return body;
    }

}
