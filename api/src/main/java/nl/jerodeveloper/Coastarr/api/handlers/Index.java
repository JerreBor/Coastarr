package nl.jerodeveloper.coastarr.api.handlers;

import nl.jerodeveloper.coastarr.api.annotations.*;

@Handler(requestType = RequestType.ALL, returnType = ReturnType.TEXT, route = "/")
public class Index {

    @Handle
    public Response handle() {
        return Response.builder()
                .text("Hello, World!\n")
                .build();
    }

}
