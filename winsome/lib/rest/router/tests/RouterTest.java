package winsome.lib.rest.router.tests;

import winsome.common.requests.AuthenticationRequest;
import winsome.lib.rest.RESTMethod;
import winsome.lib.rest.RESTResponse;
import winsome.lib.rest.router.DeserializeRequestBody;
import winsome.lib.rest.router.ResponseCode;
import winsome.lib.rest.router.Route;

public class RouterTest {
    @Route(path = "/item/{id}/{str}/{n}", method = RESTMethod.GET)
    @DeserializeRequestBody(AuthenticationRequest.class)
    public RESTResponse f(int a, String b, int c, AuthenticationRequest body) {
        System.out.println("called f");
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);

        System.out.println(body.authToken);
        return new RESTResponse(ResponseCode.OK);
    }

    @Route(path = "/item/{id}/{str}/{n}", method = RESTMethod.POST)
    public RESTResponse f1(int a, String b, int c) {
        System.out.println("called f1");
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        return new RESTResponse(ResponseCode.ACCEPTED);
    }

    public void g() {
        System.out.println("called g");
    }

    public void h() {
        System.out.println("called h");
    }
}
