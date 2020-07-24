package nl.jerodeveloper.Coastarr.api.annotations;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
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

    public HandlerLoader(HttpServer httpServer) {
        this.httpServer = httpServer;
        this.logger = Logger.getLogger("handlerfinder");
    }

    public HttpServer load() {
        Reflections reflections = new Reflections("nl.jerodeveloper.coastarr.api.handlers");
        reflections.getTypesAnnotatedWith(Handler.class, true).forEach(handlerClass -> {
            Handler handler = handlerClass.getAnnotation(Handler.class);

            String route = handler.context().equals("") ? "/api/" + handlerClass.getCanonicalName().replaceFirst("nl.jerodeveloper.coastarr.api.handlers.", "").replaceAll("\\.", "/") : handler.context();
            logger.info(String.format("Found handler %s with route %s", handlerClass.getSimpleName(), route));

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

            Method finalMethod = method;
            httpServer.createContext(handler.context().equals("") ? route : handler.context(), exchange -> {
                if (!exchange.getRequestURI().getPath().equals(route) ||
                        (!Arrays.asList(handler.requestType()).contains(RequestType.ALL) &&
                        !Arrays.asList(handler.requestType()).contains(RequestType.valueOf(exchange.getRequestMethod().toUpperCase())))) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
                    return;
                }

                exchange.getResponseHeaders().add("Content-Type", handler.returnType().getHeader());
                exchange.getResponseHeaders().add("X-Content-Type-Options", "nosniff");
                invokeMethod(finalMethod, handlerClass, exchange);
                exchange.close();
            });
            logger.info("Succesfully registered " + route + "!");
        });
        return httpServer;
    }

    private void invokeMethod(Method method, Class<?> handlerClass, HttpExchange exchange) {
        List<Object> invokeObjects = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            if (parameter.getType() == HttpHandler.class) {
                invokeObjects.add(exchange);
            }
        }

        try {
            method.invoke(handlerClass, invokeObjects.toArray());
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.severe("Something went wrong while invoking method: " + method.getName() + " in class " + handlerClass.getCanonicalName());
            e.printStackTrace();
        }
    }

}
