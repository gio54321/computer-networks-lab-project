package winsome.server;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import winsome.lib.http.HTTPParsingException;
import winsome.lib.http.HTTPRequest;

public class RequestBuffer {
    private Charset charset;
    private String buffer = "";
    private HTTPRequest request = new HTTPRequest();
    private boolean headerParsed = false;
    private int headerLength = 0;

    // TODO doc
    public RequestBuffer(Charset charset) {
        this.charset = charset;
    }

    public String getBuffer() {
        return this.buffer;
    }

    public void addToBuffer(ByteBuffer buf) {
        buf.flip();
        this.buffer += this.charset.decode(buf).toString();
        buf.flip();
    }

    public boolean messageDone() {
        var contentLength = request.getHeaders().get("Content-Length");
        if (!this.headerParsed)
            return false;
        if (this.headerParsed && contentLength == null) {
            return true;
        }
        return this.buffer.length() == Integer.parseInt(contentLength) + this.headerLength + 4;
    }

    public void partialParse() {
        if (this.buffer.contains("\r\n\r\n")) {
            var parts = this.buffer.split("\r\n\r\n");
            var firstAndRest = parts[0].split("\r\n", 2);
            var firstLine = firstAndRest[0];
            var headerLines = firstAndRest[1].split("\r\n");

            try {
                this.request.parseStartLine(firstLine);
                this.request.parseHeaders(headerLines);
            } catch (HTTPParsingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.headerParsed = true;
            this.headerLength = parts[0].length();
        }
    }
}
