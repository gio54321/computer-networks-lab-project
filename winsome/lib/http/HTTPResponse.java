package winsome.lib.http;

public class HTTPResponse extends HTTPMessage {
    private HTTPResponseCode responseCode;

    public HTTPResponse(HTTPResponseCode responseCode) {
        this.responseCode = responseCode;
        this.body = null;
    }

    public String getFormattedStartLine() {
        return this.HTTPVersion + " " + this.responseCode.getCodeAndReason();
    }

    public void parseStartLine(String line) throws HTTPParsingException {
        // parse the status line
        var tokens = line.split(" ");
        if (tokens.length != 3) {
            throw new HTTPParsingException();
        }

        this.HTTPVersion = tokens[0];
        this.responseCode = HTTPResponseCode.parseFromString(tokens[1]);
    }

    public HTTPResponseCode getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(HTTPResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    public HTTPResponse setBody(String body) {
        this.body = body;
        this.setHeader("Content-Length", Integer.toString(body.length()));
        return this;
    }

    public HTTPResponse setHeader(String key, String value) {
        if (!this.headers.containsKey(key)) {
            this.headers.put(key, value);
        } else {
            this.headers.replace(key, value);
        }
        return this;
    }
}
