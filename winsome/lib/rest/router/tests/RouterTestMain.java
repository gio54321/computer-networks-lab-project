package winsome.lib.rest.router.tests;

import winsome.lib.rest.RESTMethod;
import winsome.lib.rest.RESTRequest;
import winsome.lib.rest.router.InvalidRouteAnnotation;
import winsome.lib.rest.router.Router;

public class RouterTestMain {
    public static void main(String[] args) {

        RouterTest ta = new RouterTest();
        Router r;
        try {
            r = new Router(ta);
            var request = new RESTRequest();
            request.setPath("/item/32/ciaooo/10");
            request.setMethod(RESTMethod.GET);
            request.setBody("{\"authToken\":\"authhh\"}");
            r.callAction(request);
        } catch (InvalidRouteAnnotation e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
