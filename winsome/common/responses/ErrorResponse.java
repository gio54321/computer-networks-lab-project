package winsome.common.responses;

public class ErrorResponse extends Response {
    public String reason;

    public static ErrorResponse from(String reason) {
        var res = new ErrorResponse();
        res.reason = reason;
        return res;
    }
}
