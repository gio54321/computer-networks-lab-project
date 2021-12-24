package winsome.lib.rest.router.tests;

import winsome.lib.rest.router.Route;

public class RouterTest {
    @Route("/item/{id}/{str}/{n}")
    public void f(int a, String b, int c) {
        System.out.println("called f");
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
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
