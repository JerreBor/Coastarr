package nl.jerodeveloper.coastarr.api;

import com.sun.net.httpserver.HttpServer;
import lombok.Getter;
import nl.jerodeveloper.coastarr.api.annotations.Handler;
import nl.jerodeveloper.coastarr.api.annotations.HandlerLoader;
import nl.jerodeveloper.coastarr.api.handlers.IndexHandler;
import nl.jerodeveloper.coastarr.api.handlers.api.Status;
import nl.jerodeveloper.coastarr.api.objects.ServerState;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

public class Coastarr {

    @Getter private static HandlerLoader handlerLoader;

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8000), 0);

        Logger logger  = Logger.getLogger("http");
        logger.info("Starting http server...");

        AtomicReference<ServerState> serverState = new AtomicReference<>(ServerState.STARTING);

        HttpServer finalHttpServer = httpServer;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down http server...");
            serverState.set(ServerState.STOPPING);
            finalHttpServer.stop(2);
            logger.info("Http server stopped");
        }));

        httpServer.setExecutor(Executors.newCachedThreadPool());

        handlerLoader = new HandlerLoader(httpServer);

        // Register handlers

        handler(IndexHandler::new);
        handler(() -> new Status(serverState));

        httpServer = handlerLoader.load();

        httpServer.start();
        serverState.set(ServerState.HEALTHY);
        System.out.println("Succesfully started http server on port 8000");
    }

    private static void handler(Supplier<Object> handlerSupplier) {
        Object object = requireNonNull(requireNonNull(handlerSupplier.get(), "Object cannot be null"));

        if (!object.getClass().isAnnotationPresent(Handler.class)) {
            throw new RuntimeException("Object does not have Handler annotation.");
        }

        handlerLoader.getHandlerList().add(object);
    }

}
