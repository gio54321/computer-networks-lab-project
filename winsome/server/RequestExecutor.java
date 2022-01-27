package winsome.server;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.ByteBuffer;

import winsome.lib.http.HTTPRequest;
import winsome.lib.router.Router;

/**
 * Runnable that process a REST request
 */
public class RequestExecutor implements Runnable {
    private Router serverRouter;
    private HTTPRequest requestToBeProcessed;
    private Selector serverSelector;
    private SelectionKey clientKey;

    /**
     * The request executor constructor takes as parameters the server router and
     * the
     * request to be processed
     * It takes also the server selector and the selection key of the client
     * Caller must ensure that the client key is not in the selector interest set
     * 
     * @param serverRouter
     * @param requestToBeProcessed
     * @param serverSelector
     * @param clientKey
     */
    public RequestExecutor(Router serverRouter, HTTPRequest requestToBeProcessed, Selector serverSelector,
            SelectionKey clientKey) {
        if (serverRouter == null || requestToBeProcessed == null || serverSelector == null || clientKey == null) {
            throw new NullPointerException();
        }
        this.serverRouter = serverRouter;
        this.requestToBeProcessed = requestToBeProcessed;
        this.serverSelector = serverSelector;
        this.clientKey = clientKey;
    }

    public void run() {
        // execute the request
        var response = this.serverRouter.callAction(requestToBeProcessed);

        // attach to the client a new buffer containing the response
        clientKey.attach(ByteBuffer.wrap(response.getFormattedMessage().getBytes()));

        // put the client key back in the selector interest set, so
        // set the client interest op to write
        clientKey.interestOps(SelectionKey.OP_WRITE);

        // wakeup the server selector
        // this is very important because otherwise the change to the interest
        // set will be processed the next time the selector will wake up
        this.serverSelector.wakeup();
    }

}
