package nl.jerodeveloper.coastarr.api.annotations;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
@Setter
public class Request {

    private String requestBody;
    private Map<String, String> parameters;
    private Map<String, List<String>> headers;

}
