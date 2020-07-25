package nl.jerodeveloper.coastarr.api.handlers.api.auth;

import nl.jerodeveloper.coastarr.api.annotations.*;

@Handler
public class User {

    @Handle(requestType = RequestType.POST, returnType = ReturnType.JSON, authorization = AuthorizationType.BASIC)
    public Response post() {

        return Response.builder()
                .json("")
                .build();
    }

    @Handle(requestType = RequestType.GET, returnType = ReturnType.JSON)
    public Response get() {
        return Response.builder().json("").build();
    }

}
