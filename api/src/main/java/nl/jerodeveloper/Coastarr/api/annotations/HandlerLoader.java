package nl.jerodeveloper.coastarr.api.annotations;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.AccessLevel;
import lombok.Getter;
import nl.jerodeveloper.coastarr.api.filters.Logging;
import nl.jerodeveloper.coastarr.api.filters.Tracing;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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

            String route = handler.context().equals("") ? "/" + handlerClass
                    .getCanonicalName()
                    .replaceFirst("nl.jerodeveloper.coastarr.api.handlers.", "")
                    .replaceAll("\\.", "/").toLowerCase() : handler.context();
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

            httpServer.createContext(handler.context().equals("") ? route : handler.context(), exchange(handler, route, method, objectInHandlerList))
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
            invokeMethod(method, exchange, toInvoke);
            exchange.close();
        };
    }

    private void invokeMethod(Method method, HttpExchange exchange, Object toInvoke) {
/*        List<Object> invokeObjects = new ArrayList<>();
        for (Class<?> parameter : method.getParameterTypes()) {
            if (parameter == HttpHandler.class) {
                invokeObjects.add(exchange);
            }
        }*/

        try {
            method.invoke(toInvoke, exchange);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.severe("Something went wrong while invoking method: " + method.getName() + " in class " + toInvoke.getClass().getCanonicalName());
            e.printStackTrace();
        }
    }

}
