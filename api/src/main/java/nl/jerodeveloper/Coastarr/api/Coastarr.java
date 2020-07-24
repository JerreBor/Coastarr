package nl.jerodeveloper.Coastarr.api;

import com.sun.net.httpserver.HttpServer;
import nl.jerodeveloper.Coastarr.api.annotations.HandlerLoader;
import nl.jerodeveloper.Coastarr.api.objects.ServerState;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class Coastarr {

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

        httpServer = new HandlerLoader(httpServer).load();
        httpServer.start();
        serverState.set(ServerState.HEALTHY);
        System.out.println("Succesfully started http server on port 8000");
    }

}
