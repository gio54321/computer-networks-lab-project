package winsome.lib.router;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import winsome.common.requests.RequestModel;

/**
 * Methods annotated with this annotation will accept a request as parameter
 * and the request body will be deserialized
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DeserializeRequestBody {
    // NOTE: the class has to extend Request to restrict
    // this field to valid deserializable requests
    public Class<? extends RequestModel> value();

}
