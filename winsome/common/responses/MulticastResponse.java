package winsome.common.responses;

/*
 * Response model for multicast information
 * Used for response body serialization
 */
public class MulticastResponse extends Response {
    public String multicastAddress;
    public int port;
}
