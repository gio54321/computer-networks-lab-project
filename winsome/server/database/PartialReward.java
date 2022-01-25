package winsome.server.database;

public class PartialReward {
    private long timestamp;
    private double partialReward;

    public PartialReward(long timestamp, double partialReward) {
        this.timestamp = timestamp;
        this.partialReward = partialReward;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getPartialReward() {
        return partialReward;
    }

    public void setPartialReward(double partialReward) {
        this.partialReward = partialReward;
    }

}
