package winsome.lib.http;

public class HTTPRequest extends HTTPMessage {
    private String HTTPVersion = "HTTP/1.1";
    private String contentType = "application/json";
    private HTTPMethod method;
    private String path;
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
        if (this.body != null) {
            outStr += "Content-Type: " + this.contentType + "\r\n\r\n" + this.body;
        }

        return outStr;
    }

    // TODO doc and supported headers
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
                case "GET":
                case "POST":
                case "PUT":
                case "DELETE":
                    if (tokens.length != 3) {
                        throw new HTTPParsingException();
                    }
                    this.method = HTTPMethod.parseFromString(tokens[0]);
                    this.path = tokens[1];
                    System.out.println("path" + this.path + " " + this.method);
                    // TODO do something with http version
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
}
