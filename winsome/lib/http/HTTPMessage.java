package winsome.lib.http;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class representing an HTTP message.
 */
public abstract class HTTPMessage {
    // the http version
    protected String HTTPVersion = "HTTP/1.1";
    // the headers map
    protected Map<String, String> headers = new HashMap<>();
    // the body of the message
    protected String body;

    /**
     * Get the start line of the message formatted as described in RFC 7230
     * 
     * @return the formatted start line
     */
    public abstract String getFormattedStartLine();

    /**
     * Parse the start line of the message as described in RFC 7230
     * 
     * @param line the line to be parsed
     * @throws HTTPParsingException
     */
    public abstract void parseStartLine(String line) throws HTTPParsingException;

    /**
     * Get the entire message formatted, that is ready to be sent
     * 
     * @return the formatted message
     */
    public String getFormattedMessage() {
        // get the start line
        var outStr = this.getFormattedStartLine() + "\r\n";

        // get the headers
        for (var k : this.headers.keySet()) {
            outStr += k + ": " + this.headers.get(k) + "\r\n";
        }

        outStr += "\r\n";

        // get the body, if present
        if (this.body != null) {
            outStr += this.body;
        }
        return outStr;
    }

    /**
     * Parse the headers of an HTTP message from the header lines,
     * as described in RFC 7230
     * 
     * @param headerLines the array of header lines
     * @throws HTTPParsingException if the header lines are malformed
     */
    public void parseHeaders(String[] headerLines) throws HTTPParsingException {
        if (headerLines == null) {
            throw new NullPointerException();
        }

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

    /**
     * Get the headers map
     * 
     * @return the header's map
     */
    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    /**
     * Get the body of the message
     * 
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * Parse the body of the message
     * 
     * @param body the message body
     */
    public void parseBody(String body) {
        this.body = body;
    }

    /**
     * This method is used by subclasses to set the body of an HTTP message
     * and consequently update the Content-length header to the length
     * of the new body
     * This is done because subclasses do not have access to the headers map
     * 
     * @param body
     */
    protected void setBodySuper(String body) {
        this.body = body;
        if (body == null) {
            this.setHeaderSuper("Content-Length", null);
        } else {
            this.setHeaderSuper("Content-Length", Integer.toString(body.length()));
        }
    }

    /**
     * This method is used by subclasses to set a header key value
     * pair. If the value is null, then the header entry is removed
     * from the map.
     * This is done because subclasses do not have access to the headers map
     * 
     * @param key   the key of the header
     * @param value the value of the header
     */
    protected void setHeaderSuper(String key, String value) {
        if (value == null) {
            this.headers.remove(key);
        } else {
            if (!this.headers.containsKey(key)) {
                this.headers.put(key, value);
            } else {
                this.headers.replace(key, value);
            }
        }
    }

}
