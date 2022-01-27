package winsome.common.responses;

/*
 * Response model for multicast information
 * Used for response body serialization
 */
public class MulticastResponse extends ResponseModel {
    public String multicastAddress;
    public int port;
}
