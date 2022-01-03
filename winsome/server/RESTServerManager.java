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

import winsome.lib.router.InvalidRouteAnnotationException;
import winsome.lib.router.Router;

public class RESTServerManager {
    private ServerSocketChannel socketChannel;
    private Selector selector;
    private ByteBuffer readBuffer;
    private Router router;

    // TODO 8 only for testing
    private final int BUF_CAPACITY = 32;

    public RESTServerManager(InetSocketAddress address, Router router)
            throws IOException, InvalidRouteAnnotationException {
        // TODO doc
        this.socketChannel = ServerSocketChannel.open();
        this.socketChannel.bind(address);
        this.socketChannel.configureBlocking(false);

        this.selector = Selector.open();

        this.socketChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        this.router = router;
    }

    public void serve() throws IOException {
        // TODO global read/write buffer?
        this.readBuffer = ByteBuffer.allocate(BUF_CAPACITY);

        for (;;) {
            this.selector.select();

            // TODO explain why the iterator
            // ConcurrentModificationException
            var iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                var currentKey = iterator.next();
                if (currentKey.isAcceptable()) {
                    handleAccept();
                } else if (currentKey.isReadable()) {
                    handleRead(currentKey);
                } else if (currentKey.isWritable()) {
                    handleWrite(currentKey);

                }
                iterator.remove();
            }
        }
    }

    private void handleAccept() throws IOException, ClosedChannelException {
        var clientSocket = this.socketChannel.accept();

        clientSocket.configureBlocking(false);
        var clientKey = clientSocket.register(selector, SelectionKey.OP_READ);
        initializeClient(clientKey);
        System.out.println("new client " + clientSocket.getRemoteAddress().toString());
    }

    private void handleRead(SelectionKey clientKey) throws IOException {
        var clientChannel = (SocketChannel) clientKey.channel();
        var reqBuffer = (RequestBuffer) clientKey.attachment();

        this.readBuffer.clear();
        int bytesRead = clientChannel.read(this.readBuffer);

        reqBuffer.addToBuffer(this.readBuffer);
        reqBuffer.partialParse();

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
        System.out.println(responseBuffer.toString());

        clientChannel.write(responseBuffer);
        if (!responseBuffer.hasRemaining()) {
            initializeClient(clientKey);
        }
    }

    private void processRequest(SelectionKey clientKey) {
        var reqBuffer = (RequestBuffer) clientKey.attachment();
        clientKey.interestOps(0);
        System.out.println("received " + reqBuffer.getBuffer());
        System.out.println(reqBuffer.getRequest().getBody());
        var response = this.router.callAction(reqBuffer.getRequest());
        clientKey.attach(ByteBuffer.wrap(response.getFormattedMessage().getBytes()));
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
