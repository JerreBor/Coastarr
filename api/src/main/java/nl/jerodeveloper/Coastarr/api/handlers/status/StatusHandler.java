package nl.jerodeveloper.Coastarr.api.handlers.status;

import com.sun.net.httpserver.HttpExchange;
import nl.jerodeveloper.Coastarr.api.Constants;
import nl.jerodeveloper.Coastarr.api.annotations.Handle;
import nl.jerodeveloper.Coastarr.api.annotations.Handler;
import nl.jerodeveloper.Coastarr.api.annotations.RequestType;
import nl.jerodeveloper.Coastarr.api.annotations.ReturnType;
import nl.jerodeveloper.Coastarr.api.objects.ServerState;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicReference;

@Handler(returnType = ReturnType.JSON, requestType = RequestType.GET, context = "/api/status")
public class StatusHandler {

    private final AtomicReference<ServerState> serverState;

    public StatusHandler(AtomicReference<ServerState> serverState) {
        this.serverState = serverState;
    }

    @Handle
    public void handle(HttpExchange exchange) throws IOException {
        String json = Constants.INSTANCE.getGson().toJson(serverState.get());
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
        exchange.getResponseBody().write(json.getBytes());
    }

}
