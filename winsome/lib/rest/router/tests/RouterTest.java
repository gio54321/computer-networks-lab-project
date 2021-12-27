package winsome.lib.rest.router.tests;

import winsome.common.requests.AuthenticationRequest;
import winsome.lib.rest.RESTMethod;
import winsome.lib.rest.router.DeserializeBody;
import winsome.lib.rest.router.Route;

public class RouterTest {
    @Route(path = "/item/{id}/{str}/{n}", method = RESTMethod.GET)
    @DeserializeBody(AuthenticationRequest.class)
    public void f(int a, String b, int c, AuthenticationRequest body) {
        System.out.println("called f");
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);

        System.out.println(body.authToken);
    }

    @Route(path = "/item/{id}/{str}/{n}", method = RESTMethod.POST)
    public void f1(int a, String b, int c) {
        System.out.println("called f1");
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
    }

    public void g() {
        System.out.println("called g");
    }

    public void h() {
        System.out.println("called h");
    }
}
