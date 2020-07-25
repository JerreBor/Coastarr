package nl.jerodeveloper.coastarr.api.annotations;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.Getter;
import nl.jerodeveloper.coastarr.api.Constants;
import nl.jerodeveloper.coastarr.api.filters.Logging;
import nl.jerodeveloper.coastarr.api.filters.Tracing;
import nl.jerodeveloper.coastarr.api.util.AuthenticationUtil;
import nl.jerodeveloper.coastarr.api.util.JsonMessage;
import org.reflections.Reflections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public class HandlerLoader {

    private final HttpServer httpServer;
    private final Logger logger;
    @Getter private final List<Object> handlerList;
    private final List<Filter> filters;

    public HandlerLoader(HttpServer httpServer) {
        this.httpServer = httpServer;
        this.logger = Logger.getLogger("handlerloader");
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

            Map<String, Method> methodMap = new LinkedHashMap<>();

            for (Method methods : handlerClass.getDeclaredMethods()) {
                if (!methods.isAnnotationPresent(Handle.class)) continue;
                Handle handle = methods.getAnnotation(Handle.class);
                if (handle.requestType() == RequestType.ALL) {
                    methodMap.clear();
                    methodMap.put(methods.getAnnotation(Handle.class).requestType().name(), methods);
                    break;
                } else {
                    methodMap.put(methods.getAnnotation(Handle.class).requestType().name(), methods);
                }
            }

            if (methodMap.isEmpty()) {
                logger.warning("Could not find any method annotated with the Handle annotation in class " + handlerClass.getCanonicalName());
                return;
            }

            for (Map.Entry<String, Method> methodEntry : methodMap.entrySet()) {
                if (methodEntry.getValue().getReturnType() != Response.class) {
                    methodMap.remove(methodEntry.getKey());
                    logger.warning(methodEntry.getKey() + " method does not return Response type.");
                }
            }

            httpServer.createContext(handler.route().equals("") ? route : handler.route(), exchange(route, methodMap, objectInHandlerList))
                    .getFilters().addAll(filters);

            logger.info("Succesfully registered " + route + "!");
        });
        return httpServer;
    }

    private HttpHandler exchange(String route, Map<String, Method> methodMap, Object toInvoke) {
        return exchange -> {
            if (!exchange.getRequestURI().getPath().equals(route)) {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
                return;
            }

            Method method;

            if (methodMap.size() == 1 && methodMap.values().iterator().next().getAnnotation(Handle.class).requestType() == RequestType.ALL) {
                method = methodMap.values().iterator().next();
            } else {
                method = methodMap.get(exchange.getRequestMethod());
            }

            if (method == null) {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
                return;
            }

            Handle handle = method.getAnnotation(Handle.class);

            InputStreamReader inputStreamReader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            int b;
            StringBuilder buf = new StringBuilder(512);
            while ((b = bufferedReader.read()) != -1) {
                buf.append((char) b);
            }

            bufferedReader.close();
            inputStreamReader.close();

            Map<String, String> parameters = new LinkedHashMap<>();

            if (exchange.getRequestURI().getQuery() != null) {
                for (String param : exchange.getRequestURI().getQuery().split("&")) {
                    String[] entry = param.split("=");
                    if (entry.length > 1) {
                        parameters.put(entry[0], entry[1]);
                    } else {
                        parameters.put(entry[0], "");
                    }
                }
            }

            Request request = Request.builder()
                    .requestBody(buf.toString())
                    .headers(exchange.getRequestHeaders())
                    .parameters(parameters)
                    .build();

            exchange.getResponseHeaders().add("Content-Type", handle.returnType().getHeader());
            exchange.getResponseHeaders().add("X-Content-Type-Options", "nosniff");

            if (handle.authorization() != AuthorizationType.NONE) {
                AuthenticationUtil authenticationUtil = new AuthenticationUtil();
                List<String> authHeader = request.getHeaders().get("Authorization");
                if (authHeader == null || authHeader.isEmpty() || (!authHeader.get(0).startsWith("Bearer ") && !authHeader.get(0).startsWith("Basic "))) {
                    sendUnauthorized(exchange, "Please include an authorization header.");
                    return;
                }

                switch (handle.authorization()) {
                    case BEARER -> {
                        if (!authHeader.get(0).startsWith("Bearer ")) {
                            sendUnauthorized(exchange, "Please use a Bearer authentication header.");
                            return;
                        }

                        if (!authenticationUtil.verifyToken(authHeader.get(0).substring("Bearer ".length()))) {
                            sendUnauthorized(exchange, "Token invalid");
                            return;
                        }
                    }
                    case BASIC -> {
                        if (!authHeader.get(0).startsWith("Basic ")) {
                            sendUnauthorized(exchange, "Please use a Basic authentication header.");
                            return;
                        }

                        // TODO implement
                    }
                }

            }

            Response response = invokeMethod(method, exchange, request, toInvoke);

            if (response == null) {
                writeError(exchange, new NullPointerException("Response was not specified"));
            } else {
                switch (handle.returnType()) {
                    case JSON -> {
                        if (response.getJson() == null) {
                            writeError(exchange, new IllegalArgumentException(String.format("%s method in class %s with ReturnType of JSON does not return JSON response.", method.getName(), method.getDeclaringClass().getCanonicalName())));
                            break;
                        }

                        sendResponseHeaders(exchange, response);
                        exchange.getResponseBody().write(response.getJson().getBytes());
                    }
                    case TEXT -> {
                        if (response.getText() == null) {
                            writeError(exchange, new IllegalArgumentException(String.format("%s method in class %s with ReturnType of TEXT does not return TEXT response.", method.getName(), method.getDeclaringClass().getCanonicalName())));
                            break;
                        }

                        sendResponseHeaders(exchange, response);
                        exchange.getResponseBody().write(response.getText().getBytes());
                    }
                }
            }

            exchange.close();
        };
    }

    private void sendUnauthorized(HttpExchange exchange, String msg) throws IOException {
        String message = Constants.INSTANCE.getGson().toJson(new JsonMessage(msg));
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_UNAUTHORIZED, message.getBytes().length);
        exchange.getResponseBody().write(message.getBytes());
        exchange.close();
    }

    private void writeError(HttpExchange exchange, Object error) throws IOException {
        String jsonError = Constants.INSTANCE.getGson().toJson(error);
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, jsonError.getBytes().length);
        exchange.getResponseBody().write(jsonError.getBytes());
        logger.severe(jsonError);
    }

    private void sendResponseHeaders(HttpExchange exchange, Response response) throws IOException {
        if (response.getCode() != 0) {
            exchange.sendResponseHeaders(response.getCode(),
                    response.getJson() == null ? (response.getText() == null ? 0 : response.getText().length()) : response.getJson().length());
        } else {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK,
                    response.getJson() == null ? (response.getText() == null ? 0 : response.getText().length()) : response.getJson().length());
        }
    }

    private Response invokeMethod(Method method, HttpExchange exchange, Request request, Object toInvoke) {
        try {
            List<Object> parameters = new LinkedList<>();
            for (Class<?> parameterType : method.getParameterTypes()) {
                if (HttpExchange.class.equals(parameterType)) {
                    parameters.add(exchange);
                } else if (Request.class.equals(parameterType)) {
                    parameters.add(request);
                }
            }
            return (Response) method.invoke(toInvoke, parameters.isEmpty() ? null : parameters.toArray());
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.severe("Something went wrong while invoking method: " + method.getName() + " in class " + toInvoke.getClass().getCanonicalName());
            e.printStackTrace();
            return null;
        }
    }

}
