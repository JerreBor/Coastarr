package nl.jerodeveloper.coastarr.api.annotations;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import nl.jerodeveloper.coastarr.api.Constants;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class Response {

    private String text;
    private Object json;
    private int code;

    public String getJson() {
        return Constants.INSTANCE.getGson().toJson(json);
    }

}
