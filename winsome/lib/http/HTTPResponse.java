package winsome.lib.http;

public class HTTPResponse extends HTTPMessage {
    private String HTTPVersion = "HTTP/1.1";
    private String contentType = "application/json";
    private HTTPResponseCode responseCode;
    private String body;

    public HTTPResponse() {

    }

    // TODO doc default version and content type
    public HTTPResponse(HTTPResponseCode responseCode, String body) {
        this.responseCode = responseCode;
        this.body = body;
    }

    public String getFormattedMessage() {
        var outStr = this.HTTPVersion + " " + this.responseCode.getFullStatusLine() + "\r\n";
        if (this.body != null) {
            outStr += "Content-Type: " + this.contentType + "\r\n\r\n" + this.body;
        }

        return outStr;
    }

    public void parseFormattedMessage(String message) throws HTTPParsingException {
        if (message == null) {
            throw new NullPointerException();
        }

        var lines = message.split("\r\n");
        var i = 0;
        // parse headers
        while (lines[i].length() != 0) {
            System.out.println("* " + lines[i] + lines[i].length());
            var tokens = lines[i].split(" ");
            switch (tokens[0]) {
                case "HTTP/1.1":
                    if (tokens.length < 2) {
                        throw new HTTPParsingException();
                    }
                    this.responseCode = HTTPResponseCode.parseFromString(tokens[1]);
                    System.out.println("code" + this.responseCode);
                    // TODO chek for the phrase?
                    break;

                case "Content-Type:":
                    System.out.println(lines[i]);
                    if (tokens.length != 2) {
                        throw new HTTPParsingException();
                    }
                    this.contentType = tokens[1];
                    System.out.println("content type " + this.contentType);
                    break;
            }
            i++;
        }

        // parse the request body
        if (i < lines.length) {
            this.body = "";
            for (; i < lines.length; ++i) {
                this.body += "\r\n" + lines[i];
            }
            this.body = this.body.trim();
        } else {
            this.body = null;
        }
    }

    public HTTPResponse(HTTPResponseCode responseCode) {
        this.responseCode = responseCode;
        this.body = null;
    }

    public HTTPResponseCode getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(HTTPResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
