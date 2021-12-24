package winsome.lib.rest.router.tests;

import winsome.lib.rest.router.Router;

public class RouterTestMain {
    public static void main(String[] args) {

        RouterTest ta = new RouterTest();
        Router r = new Router(ta);
        r.callAction("/item/32/ciaooo/10");
    }
}
