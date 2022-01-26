package winsome.server.database;

import winsome.server.database.serializables.SerializablePartialReward;

/**
 * Class that represent a partial reward, given by a timestamp and a partial
 * reward amunt
 */
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

    /**
     * Clone object to a serializable version of it
     * 
     * @return the cloned serializable object
     */
    public SerializablePartialReward cloneToSerializable() {
        var out = new SerializablePartialReward();
        out.timestamp = this.timestamp;
        out.partialReward = this.partialReward;
        return out;
    }

    /**
     * Clone serializable object into this
     * 
     * @param partialReward the serializable object
     */
    public void fromSerializable(SerializablePartialReward partialReward) {
        this.timestamp = partialReward.timestamp;
        this.partialReward = partialReward.partialReward;
    }

}
