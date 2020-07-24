package nl.jerodeveloper.Coastarr.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import nl.jerodeveloper.Coastarr.api.annotations.Handle;
import nl.jerodeveloper.Coastarr.api.annotations.Handler;
import nl.jerodeveloper.Coastarr.api.annotations.RequestType;
import nl.jerodeveloper.Coastarr.api.annotations.ReturnType;

import java.io.IOException;
import java.net.HttpURLConnection;

@Handler(requestType = RequestType.ALL, returnType = ReturnType.TEXT, context = "/")
public class IndexHandler {

    @Handle
    public void handle(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, 0);
        exchange.getResponseBody().write("Hello, World!\n".getBytes());
    }

}
