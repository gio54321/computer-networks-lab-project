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

    /**
     * Get the formatted start line
     * 
     * @return the formatted start line
     */
    public String getFormattedStartLine() {
        return this.HTTPVersion + " " + this.responseCode.getCodeAndReason();
    }

    /**
     * Parse the start line. In particular it parses the HTTP version and the
     * response code
     * 
     * @param line the line to be parsed
     */
    public void parseStartLine(String line) throws HTTPParsingException {
        // parse the status line
        var tokens = line.split(" ");
        if (tokens.length < 3) {
            throw new HTTPParsingException();
        }

        this.HTTPVersion = tokens[0];
        this.responseCode = HTTPResponseCode.parseFromString(tokens[1]);
    }

    /**
     * Get the response code of the response
     * 
     * @return the response code
     */
    public HTTPResponseCode getResponseCode() {
        return responseCode;
    }

    /**
     * Set the response code of the response
     * 
     * @param responseCode
     */
    public void setResponseCode(HTTPResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * Set the response body. This affects the Content-length header
     * that is set equal to the length of the new body
     * 
     * @param body the new body
     * @return the modfied HTTP response changed
     */
    public HTTPResponse setBody(String body) {
        super.setBodySuper(body);
        return this;
    }

    /**
     * Set the header with the new value. If the value is null then
     * the header entry is removed
     * 
     * @param key
     * @param value
     * @return the modified HTTP response
     */
    public HTTPResponse setHeader(String key, String value) {
        super.setHeaderSuper(key, value);
        return this;
    }

    /**
     * Static method to forge a new response with given Object as body.
     * The object is attempted to serialize into json
     * 
     * @param code the response code
     * @param body the body object
     * @return null if the serialization was unsuccessful, a new HTTPResponse
     *         otherwise
     */
    public static HTTPResponse response(HTTPResponseCode code, Object body) {
        var objectMapper = new ObjectMapper();
        try {
            return new HTTPResponse(code)
                    .setBody(objectMapper.writeValueAsString(body));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HTTPResponse errorResponse(HTTPResponseCode code, String reason) {
        return response(code, ErrorResponse.from(reason));
    }
}
