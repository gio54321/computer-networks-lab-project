package winsome.lib.router.tests;

import winsome.lib.http.HTTPMethod;
import winsome.lib.http.HTTPParsingException;
import winsome.lib.http.HTTPRequest;
import winsome.lib.http.HTTPResponse;
import winsome.lib.router.InvalidRouteAnnotationException;
import winsome.lib.router.Router;

public class RouterTestMain {
    public static void main(String[] args) throws HTTPParsingException {

        RouterTest ta = new RouterTest();
        Router r;
        try {
            r = new Router(ta);
            var request = new HTTPRequest();
            request.setPath("/item/32/ciaooo/10");
            request.setMethod(HTTPMethod.GET);
            request.setBody("{\"authToken\":\"authhh\"}");
            var response = r.callAction(request);
            System.out.println(response.getFormattedMessage());

            var r1 = new HTTPResponse();
            r1.parseFormattedMessage(
                    "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n{d\r\na\r\n\r\nta}");
        } catch (InvalidRouteAnnotationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
