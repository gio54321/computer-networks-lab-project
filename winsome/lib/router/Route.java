package winsome.lib.router;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import winsome.lib.http.HTTPMethod;

/**
 * Annotation used to annotate a method as the routing point for
 * a particular path and a particular HTTP method
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Route {
    public String path() default "/";

    public HTTPMethod method() default HTTPMethod.GET;
}
