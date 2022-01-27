package winsome.common.responses;

/*
 * Response model for generic error
 * Used for response body serialization
 */
public class ErrorResponse extends ResponseModel {
    public String reason;

    public static ErrorResponse from(String reason) {
        var res = new ErrorResponse();
        res.reason = reason;
        return res;
    }
}
