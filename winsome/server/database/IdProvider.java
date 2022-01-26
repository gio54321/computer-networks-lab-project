package winsome.server.database;

/**
 * A simple id provider. It has an internal state and provides with a thread
 * safe way of getting new post ids
 */
public class IdProvider {
    private int counter = 0;

    public IdProvider() {
    }

    public IdProvider(int initialCounter) {
        this.counter = initialCounter;
    }

    /**
     * Get a freshly generated post id.
     * This function is safe to call in a multi threaded environment
     * 
     * @return a new id
     */
    public synchronized int getNewId() {
        this.counter++;
        return this.counter;
    }

    /**
     * Get the current state of the id provider
     * This function has to be called in a single threaded environemt
     * 
     * @return the current state
     */
    public int getCurrentState() {
        return this.counter;
    }

    /**
     * Set the current state of the id provider
     * This function has to be called in a single threaded environemt
     * 
     */
    public void setCurrentState(int state) {
        this.counter = state;
    }
}
