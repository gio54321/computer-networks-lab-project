package winsome.lib.router;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import winsome.common.requests.Request;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DeserializeRequestBody {
    // the value has to extend Request to limit the
    // possible values
    public Class<? extends Request> value();

}
