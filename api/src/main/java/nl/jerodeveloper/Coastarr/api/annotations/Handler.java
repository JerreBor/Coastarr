package nl.jerodeveloper.Coastarr.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Handler {

    RequestType[] requestType();
    ReturnType returnType();
    String context()    default "";

}
