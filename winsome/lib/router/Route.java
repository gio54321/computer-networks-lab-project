package winsome.lib.router;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import winsome.lib.http.HTTPMethod;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Route {
    // TODO doc
    public String path() default "/";

    public HTTPMethod method() default HTTPMethod.GET;
}
