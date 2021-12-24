package winsome.lib.rest.router.tests;

import winsome.lib.rest.router.Route;

public class RouterTest {
    @Route("/item/{id}/{diaooo}/{f}")
    public void f(int a, int b, int c) {
        System.out.println("called f");
        System.out.println(a);
        System.out.println(b);
    }

    @Route("/")
    public void g() {
        System.out.println("called g");
    }

    @Route("/foo")
    public void h() {
        System.out.println("called h");
    }
}
