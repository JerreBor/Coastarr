package nl.jerodeveloper.coastarr.api.handlers;

import nl.jerodeveloper.coastarr.api.annotations.*;
import nl.jerodeveloper.coastarr.api.objects.JsonMessage;

@Handler(route = "/")
public class Index {

    @Handle(requestType = RequestType.GET, returnType = ReturnType.JSON, authorization = AuthorizationType.NONE)
    public Response handle() {
        return Response.builder()
                .json(new JsonMessage("Hello, World!"))
                .build();
    }

}
