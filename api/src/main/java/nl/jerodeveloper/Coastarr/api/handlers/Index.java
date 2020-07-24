package nl.jerodeveloper.coastarr.api.handlers;

import nl.jerodeveloper.coastarr.api.annotations.*;

@Handler(route = "/")
public class Index {

    @Handle(requestType = RequestType.GET, returnType = ReturnType.JSON)
    public Response handle() {
        return Response.builder()
                .text("Hello, World!\n")
                .build();
    }

}
