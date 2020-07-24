package nl.jerodeveloper.coastarr.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import nl.jerodeveloper.coastarr.api.annotations.Handle;
import nl.jerodeveloper.coastarr.api.annotations.Handler;
import nl.jerodeveloper.coastarr.api.annotations.RequestType;
import nl.jerodeveloper.coastarr.api.annotations.ReturnType;

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
