package winsome.lib.rest.router;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import winsome.lib.rest.RESTMethod;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Route {
    // TODO doc
    public String path() default "/";

    public RESTMethod method() default RESTMethod.GET;
}
