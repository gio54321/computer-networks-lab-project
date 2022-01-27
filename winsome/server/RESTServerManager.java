package winsome.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import winsome.lib.http.HTTPParsingException;
import winsome.lib.http.HTTPResponse;
import winsome.lib.http.HTTPResponseCode;
import winsome.lib.router.InvalidRouteAnnotationException;
import winsome.lib.router.Router;

public class RESTServerManager {
    // the server's socket channel
    private ServerSocketChannel socketChannel;
    // main server selector
    private Selector selector;
    // the main read/write buffer
    private ByteBuffer readBuffer;
    // router to handle the requests
    private Router router;

    // the read/write buffer capacity
    private final int BUF_CAPACITY = 4096;

    public RESTServerManager(InetSocketAddress address, Router router)
            throws IOException, InvalidRouteAnnotationException {
        if (address == null || router == null) {
            throw new NullPointerException();
        }
        this.router = router;

        // open a socket channel in non blocking mode
        this.socketChannel = ServerSocketChannel.open();
        this.socketChannel.bind(address);
        this.socketChannel.configureBlocking(false);

        // open a new selector
        this.selector = Selector.open();

        // register the socket channel for accept ops
        this.socketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
    }

    public void serve() throws IOException {
        // initialize the read/write buffer
        this.readBuffer = ByteBuffer.allocate(BUF_CAPACITY);

        for (;;) {
            // the main loop is blocked at the selector
            this.selector.select();

            // iterate over the selected keys
            var iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                var currentKey = iterator.next();

                // dispatch the current key based on the ready operation
                if (currentKey.isAcceptable()) {
                    handleAccept();
                } else if (currentKey.isReadable()) {
                    handleRead(currentKey);
                } else if (currentKey.isWritable()) {
                    handleWrite(currentKey);

                }

                // we have to use the iterator's method remove because
                // if we use the set remove the iterator would throw
                // a ConcurrentModificationException
                iterator.remove();
            }
        }
    }

    private void handleAccept() throws IOException, ClosedChannelException {
        // accept the new client
        var clientSocket = this.socketChannel.accept();

        // configure the client non blocking
        clientSocket.configureBlocking(false);

        // register the client for reading on the selector
        var clientKey = clientSocket.register(selector, SelectionKey.OP_READ);

        // initalize the read buffer attached to the client
        initializeClient(clientKey);

        System.out.println("new client " + clientSocket.getRemoteAddress().toString());
    }

    private void handleRead(SelectionKey clientKey) throws IOException {
        var clientChannel = (SocketChannel) clientKey.channel();
        var reqBuffer = (RequestBuffer) clientKey.attachment();

        // read from the client channel
        this.readBuffer.clear();
        int bytesRead = clientChannel.read(this.readBuffer);

        // add the content read to the client's buffer
        reqBuffer.addToBuffer(this.readBuffer);

        // try to do partial parsing of the message
        try {
            reqBuffer.partialParse();
        } catch (HTTPParsingException e) {
            // if the request is syntactically malformed
            // send to client BAD REQUEST
            System.out.println("Bad HTTP request");

            var response = new HTTPResponse(HTTPResponseCode.BAD_REQUEST);
            clientKey.attach(ByteBuffer.wrap(response.getFormattedMessage().getBytes()));
            clientKey.interestOps(SelectionKey.OP_WRITE);
            return;
        }

        if (reqBuffer.messageDone()) {
            // process request
            this.processRequest(clientKey);
        }

        if (bytesRead == -1) {
            // EOF: client has closed the connection
            clientChannel.close();
        }
    }

    private void handleWrite(SelectionKey clientKey) throws IOException {
        var clientChannel = (SocketChannel) clientKey.channel();
        var responseBuffer = (ByteBuffer) clientKey.attachment();

        // write the content of the response buffer to the client
        clientChannel.write(responseBuffer);

        // if the message has been completely sent, re-initialize the client
        if (!responseBuffer.hasRemaining()) {
            initializeClient(clientKey);
        }
    }

    private void processRequest(SelectionKey clientKey) {
        var reqBuffer = (RequestBuffer) clientKey.attachment();

        // reset the interest ops of the client
        clientKey.interestOps(0);

        System.out.println("received " + reqBuffer.getBuffer());
        System.out.println(reqBuffer.getRequest().getBody());

        // process the response
        var response = this.router.callAction(reqBuffer.getRequest());

        // attach to the client a new buffer containing the response
        clientKey.attach(ByteBuffer.wrap(response.getFormattedMessage().getBytes()));
        // set the client interest op to write
        clientKey.interestOps(SelectionKey.OP_WRITE);
    }

    private void initializeClient(SelectionKey clientKey) {
        // NOTE: All the packets are first parsed in US_ASCII because
        // parsing them in UTF-8, as specified in RFC 7230 Section 3,
        // would result in security vulnerabilities. Furthermore the
        // Content-Length header measures the number of bytes
        // of the body, so parsing UTF-8 could lead to some discrepancy
        // between the number of bytes and the number of characters
        // in the body
        var reqBuffer = new RequestBuffer(StandardCharsets.US_ASCII);
        clientKey.attach(reqBuffer);
        clientKey.interestOps(SelectionKey.OP_READ);
    }
}
