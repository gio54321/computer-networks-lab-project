package winsome.server;

import winsome.lib.http.HTTPMethod;
import winsome.lib.http.HTTPResponse;
import winsome.lib.http.HTTPResponseCode;
import winsome.lib.router.Route;

public class RESTLogic {
    @Route(method = HTTPMethod.POST, path = "/")
    public HTTPResponse root() {
        return new HTTPResponse(HTTPResponseCode.OK).setBody("{body}");
    }
}
