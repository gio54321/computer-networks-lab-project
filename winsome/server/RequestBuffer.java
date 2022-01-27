package winsome.server;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import winsome.lib.http.HTTPParsingException;
import winsome.lib.http.HTTPRequest;

/**
 * Helper class that impelents a request buffer, that provides a wrapper over
 * the partial parsing state of an HTTP request.
 * This is necessary because the server's IO is managed by NIO non-blocking
 * channels and this requires handling partial messages.
 */
public class RequestBuffer {
    // parsing charset
    private Charset charset;
    // the partial read buffer
    private String buffer = "";
    // the parsed request
    private HTTPRequest request = new HTTPRequest();

    // flag that indicates wether the header has been parsed
    private boolean headerParsed = false;
    // the header length
    private int headerLength = 0;

    public RequestBuffer(Charset charset) {
        this.charset = charset;
    }

    public String getBuffer() {
        return this.buffer;
    }

    /**
     * Append the given byte buffer to the internal buffer
     * The content of the buffer is decoded with the given charset
     * 
     * @param buf the byte buffer
     */
    public void addToBuffer(ByteBuffer buf) {
        buf.flip();
        this.buffer += this.charset.decode(buf).toString();
        buf.flip();
    }

    /**
     * Check if the message is done reading
     * This is done by checking the headers and the Content-length (if it exist)
     * header for requests with non empty body
     * 
     * @return true if the message has been completely received
     */
    public boolean messageDone() {
        var contentLength = request.getHeaders().get("Content-Length");
        if (!this.headerParsed)
            return false;
        if (this.headerParsed && contentLength == null) {
            return true;
        }
        return this.buffer.length() == Integer.parseInt(contentLength) + this.headerLength + 4;
    }

    /**
     * Do partial parsing of the available buffer
     * In particular try to parse the start line, the headers and the body
     * 
     * @throws HTTPParsingException
     */
    public void partialParse() throws HTTPParsingException {
        // stry to split the buffer at double CRCL
        var parts = this.buffer.split("\r\n\r\n");
        if (!this.headerParsed && this.buffer.contains("\r\n\r\n")) {
            // if the header has not been parser and it has been completely read

            // get the first line and the headers lines
            var firstAndRest = parts[0].split("\r\n", 2);
            var firstLine = firstAndRest[0];

            // parse the first line
            this.request.parseStartLine(firstLine);
            if (firstAndRest.length > 1) {
                var headerLines = firstAndRest[1].split("\r\n");
                this.request.parseHeaders(headerLines);
            }

            this.headerParsed = true;
            this.headerLength = parts[0].length();

        }
        // if the message has been completely received parse the body
        if (this.messageDone() && parts.length > 1) {
            this.request.parseBody(parts[1]);
        }
    }

    /**
     * Get the parsed HTTP request
     * 
     * @return the HTTP request
     */
    public HTTPRequest getRequest() {
        return request;
    }
}
