package winsome.lib.router.tests;

import winsome.common.requests.AuthenticationRequest;
import winsome.lib.http.HTTPMethod;
import winsome.lib.http.HTTPResponse;
import winsome.lib.router.DeserializeRequestBody;
import winsome.lib.router.ResponseCode;
import winsome.lib.router.Route;

public class RouterTest {
    @Route(path = "/item/{id}/{str}/{n}", method = HTTPMethod.GET)
    @DeserializeRequestBody(AuthenticationRequest.class)
    public HTTPResponse f(int a, String b, int c, AuthenticationRequest body) {
        System.out.println("called f");
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);

        System.out.println(body.authToken);
        return new HTTPResponse(ResponseCode.OK);
    }

    @Route(path = "/item/{id}/{str}/{n}", method = HTTPMethod.POST)
    public HTTPResponse f1(int a, String b, int c) {
        System.out.println("called f1");
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        return new HTTPResponse(ResponseCode.ACCEPTED);
    }

    public void g() {
        System.out.println("called g");
    }

    public void h() {
        System.out.println("called h");
    }
}
