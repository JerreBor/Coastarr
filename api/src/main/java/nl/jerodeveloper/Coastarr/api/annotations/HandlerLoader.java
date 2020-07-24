package nl.jerodeveloper.coastarr.api.annotations;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.Getter;
import nl.jerodeveloper.coastarr.api.filters.Logging;
import nl.jerodeveloper.coastarr.api.filters.Tracing;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class HandlerLoader {

    private final HttpServer httpServer;
    private final Logger logger;
    @Getter private final List<Object> handlerList;
    private final List<Filter> filters;

    public HandlerLoader(HttpServer httpServer) {
        this.httpServer = httpServer;
        this.logger = Logger.getLogger("handlerfinder");
        this.handlerList = new ArrayList<>();
        this.filters = Arrays.asList(new Logging(Logger.getLogger("http")), new Tracing(() -> Long.toString(System.nanoTime())));
    }

    public HttpServer load() {
        Reflections reflections = new Reflections("nl.jerodeveloper.coastarr.api.handlers");
        reflections.getTypesAnnotatedWith(Handler.class, true).forEach(handlerClass -> {
            Handler handler = handlerClass.getAnnotation(Handler.class);

            String route = handler.route().equals("") ? "/" + handlerClass
                    .getCanonicalName()
                    .replaceFirst("nl.jerodeveloper.coastarr.api.handlers.", "")
                    .replaceAll("\\.", "/").toLowerCase() : handler.route();
            logger.info(String.format("Found handler %s with route %s", handlerClass.getSimpleName(), route));

            Object objectInHandlerList = null;

            for (Object object : handlerList) {
                if (object.getClass() == handlerClass) {
                    objectInHandlerList = object;
                    break;
                }
            }

            if (objectInHandlerList == null) {
                logger.warning(String.format("Found handler %s but could not find object in handler-list", handlerClass));
                return;
            }

            Method method = null;

            for (Method methods : handlerClass.getDeclaredMethods()) {
                if (methods.isAnnotationPresent(Handle.class)) {
                    method = methods;
                }
            }

            if (method == null) {
                logger.warning("Could not find method annotated with Handle annotation in class " + handlerClass.getCanonicalName());
                return;
            }

            if (method.getReturnType() != Response.class) {
                logger.warning("Handler method in class " + handlerClass + " does not return Response");
                return;
            }

            httpServer.createContext(handler.route().equals("") ? route : handler.route(), exchange(handler, route, method, objectInHandlerList))
                    .getFilters().addAll(filters);

            logger.info("Succesfully registered " + route + "!");
        });
        return httpServer;
    }

    private HttpHandler exchange(Handler handler, String route, Method method, Object toInvoke) {
        return exchange -> {
            if (!exchange.getRequestURI().getPath().equals(route) ||
                    (!Arrays.asList(handler.requestType()).contains(RequestType.ALL) &&
                            !Arrays.asList(handler.requestType()).contains(RequestType.valueOf(exchange.getRequestMethod().toUpperCase())))) {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
                return;
            }

            exchange.getResponseHeaders().add("Content-Type", handler.returnType().getHeader());
            exchange.getResponseHeaders().add("X-Content-Type-Options", "nosniff");
            Response response = invokeMethod(method, exchange, toInvoke);

            if (response == null) {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, -1);
            } else {
                if (response.getCode() != 0) {
                    exchange.sendResponseHeaders(response.getCode(),
                            response.getJson() == null ? (response.getText() == null ? 0 : response.getText().length()) : response.getJson().length());
                } else {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK,
                            response.getJson() == null ? (response.getText() == null ? 0 : response.getText().length()) : response.getJson().length());
                }

                switch (handler.returnType()) {
                    case JSON -> {
                        if (response.getJson() == null) {
                            throw new RuntimeException(toInvoke.getClass() + " with ReturnType of JSON does not return JSON response.");
                        }

                        exchange.getResponseBody().write(response.getJson().getBytes());
                    }
                    case TEXT -> {
                        if (response.getText() == null) {
                            throw new RuntimeException(toInvoke.getClass() + " with ReturnType of TEXT does not return TEXT response.");
                        }

                        exchange.getResponseBody().write(response.getText().getBytes());
                    }
                }

            }

            exchange.close();
        };
    }

    private Response invokeMethod(Method method, HttpExchange exchange, Object toInvoke) {
/*        List<Object> invokeObjects = new ArrayList<>();
        for (Class<?> parameter : method.getParameterTypes()) {
            if (parameter == HttpHandler.class) {
                invokeObjects.add(exchange);
            }
        }*/

        try {
            if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == HttpExchange.class) {
                return (Response) method.invoke(toInvoke, exchange);
            } else {
                return (Response) method.invoke(toInvoke);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.severe("Something went wrong while invoking method: " + method.getName() + " in class " + toInvoke.getClass().getCanonicalName());
            e.printStackTrace();
            return null;
        }
    }

}
