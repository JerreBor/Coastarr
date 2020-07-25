package nl.jerodeveloper.coastarr.api.handlers.api;

import nl.jerodeveloper.coastarr.api.annotations.*;
import nl.jerodeveloper.coastarr.api.objects.ServerState;
import nl.jerodeveloper.coastarr.api.util.JsonMessage;

import java.util.concurrent.atomic.AtomicReference;

@Handler
public class Status {

    private final AtomicReference<ServerState> serverState;

    public Status(AtomicReference<ServerState> serverState) {
        this.serverState = serverState;
    }

    @Handle(requestType = RequestType.GET, returnType = ReturnType.JSON)
    public Response handle() {
        return Response.builder()
                .json(new JsonMessage(serverState.get().name()))
                .build();
    }

}
