package winsome.common.responses;

/*
 * Response model for partial reward
 * Used for response body serialization
 */
public class PartialRewardResponse extends Response {
    public long timestamp;
    public double partialReward;
}
