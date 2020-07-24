package nl.jerodeveloper.coastarr.api.handlers.api;

import nl.jerodeveloper.coastarr.api.Constants;
import nl.jerodeveloper.coastarr.api.annotations.*;
import nl.jerodeveloper.coastarr.api.objects.ServerState;

import java.util.concurrent.atomic.AtomicReference;

@Handler
public class Status {

    private final AtomicReference<ServerState> serverState;

    public Status(AtomicReference<ServerState> serverState) {
        this.serverState = serverState;
    }

    @Handle(requestType = RequestType.GET, returnType = ReturnType.JSON)
    public Response handle() {
        String json = Constants.INSTANCE.getGson().toJson(serverState.get());
        return Response.builder()
                .json(json)
                .build();
    }

}
