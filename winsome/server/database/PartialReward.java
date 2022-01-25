package winsome.server.database;

import winsome.server.database.serializables.SerializablePartialReward;

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

    public SerializablePartialReward cloneToSerializable() {
        var out = new SerializablePartialReward();
        out.timestamp = this.timestamp;
        out.partialReward = this.partialReward;
        return out;
    }

    public void fromSerializable(SerializablePartialReward comment) {
        this.timestamp = comment.timestamp;
        this.partialReward = comment.partialReward;
    }

}
