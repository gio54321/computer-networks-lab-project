package winsome.lib.router.tests;

import winsome.lib.http.HTTPMethod;
import winsome.lib.http.HTTPRequest;
import winsome.lib.router.InvalidRouteAnnotation;
import winsome.lib.router.Router;

public class RouterTestMain {
    public static void main(String[] args) {

        RouterTest ta = new RouterTest();
        Router r;
        try {
            r = new Router(ta);
            var request = new HTTPRequest();
            request.setPath("/item/32/ciaooo/10");
            request.setMethod(HTTPMethod.GET);
            request.setBody("{\"authToken\":\"authhh\"}");
            r.callAction(request);
        } catch (InvalidRouteAnnotation e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
