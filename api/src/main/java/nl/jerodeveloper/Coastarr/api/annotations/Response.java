package nl.jerodeveloper.coastarr.api.annotations;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Response {

    private String text, json;
    private int code;

}
