package winsome.lib.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import winsome.common.responses.ErrorResponse;

public class HTTPResponse extends HTTPMessage {
    private HTTPResponseCode responseCode;

    public HTTPResponse() {
    }

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
        super.setBodySuper(body);
        return this;
    }

    public HTTPResponse setHeader(String key, String value) {
        super.setHeaderSuper(key, value);
        return this;
    }

    public static HTTPResponse response(HTTPResponseCode code, Object body) {
        var objectMapper = new ObjectMapper();
        try {
            return new HTTPResponse(code)
                    .setBody(objectMapper.writeValueAsString(body));
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static HTTPResponse errorResponse(HTTPResponseCode code, String reason) {
        return response(code, ErrorResponse.from(reason));
    }
}
