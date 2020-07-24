package nl.jerodeveloper.coastarr.api.handlers.api;

import nl.jerodeveloper.coastarr.api.Constants;
import nl.jerodeveloper.coastarr.api.annotations.*;
import nl.jerodeveloper.coastarr.api.objects.ServerState;

import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicReference;

@Handler(returnType = ReturnType.JSON, requestType = RequestType.GET)
public class Status {

    private final AtomicReference<ServerState> serverState;

    public Status(AtomicReference<ServerState> serverState) {
        this.serverState = serverState;
    }

    @Handle
    public Response handle() {
        String json = Constants.INSTANCE.getGson().toJson(serverState.get());
        return Response.builder()
                .json(json)
                .build();
    }

}
