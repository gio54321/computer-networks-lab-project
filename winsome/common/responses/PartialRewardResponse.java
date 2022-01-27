package winsome.common.responses;

/*
 * Response model for partial reward
 * Used for response body serialization
 */
public class PartialRewardResponse extends ResponseModel {
    public long timestamp;
    public double partialReward;
}
