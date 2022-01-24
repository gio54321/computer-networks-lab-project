package winsome.server.database;

public class IdProvider {
    private int counter = 0;

    public IdProvider() {
    }

    public IdProvider(int initialCounter) {
        this.counter = initialCounter;
    }

    public synchronized int getNewId() {
        this.counter++;
        return this.counter;
    }
}
