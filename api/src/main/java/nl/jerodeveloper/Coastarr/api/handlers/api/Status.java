package nl.jerodeveloper.coastarr.api.handlers.api;

import com.sun.net.httpserver.HttpExchange;
import nl.jerodeveloper.coastarr.api.Constants;
import nl.jerodeveloper.coastarr.api.annotations.Handle;
import nl.jerodeveloper.coastarr.api.annotations.Handler;
import nl.jerodeveloper.coastarr.api.annotations.RequestType;
import nl.jerodeveloper.coastarr.api.annotations.ReturnType;
import nl.jerodeveloper.coastarr.api.objects.ServerState;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicReference;

@Handler(returnType = ReturnType.JSON, requestType = RequestType.GET)
public class Status {

    private final AtomicReference<ServerState> serverState;

    public Status(AtomicReference<ServerState> serverState) {
        this.serverState = serverState;
    }

    @Handle
    public void handle(HttpExchange exchange) throws IOException {
        String json = Constants.INSTANCE.getGson().toJson(serverState.get());
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
        exchange.getResponseBody().write(json.getBytes());
    }

}
