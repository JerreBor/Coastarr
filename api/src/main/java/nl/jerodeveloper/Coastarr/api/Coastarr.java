package nl.jerodeveloper.coastarr.api;

import com.sun.net.httpserver.HttpServer;
import lombok.Getter;
import nl.jerodeveloper.coastarr.api.annotations.Handler;
import nl.jerodeveloper.coastarr.api.annotations.HandlerLoader;
import nl.jerodeveloper.coastarr.api.handlers.Index;
import nl.jerodeveloper.coastarr.api.handlers.api.Status;
import nl.jerodeveloper.coastarr.api.handlers.api.auth.Token;
import nl.jerodeveloper.coastarr.api.handlers.api.auth.User;
import nl.jerodeveloper.coastarr.api.objects.ServerState;
import nl.jerodeveloper.coastarr.api.objects.Settings;
import nl.jerodeveloper.coastarr.api.util.AuthenticationUtil;
import nl.jerodeveloper.coastarr.api.util.SettingsUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

public class Coastarr {

    @Getter private static HandlerLoader handlerLoader;
    @Getter private static SettingsUtil settingsUtil;

    public static void main(String[] args) throws IOException {
        settingsUtil = new SettingsUtil();

        settingsUtil.loadSettings();

        Settings s = settingsUtil.getSettings();

        s.getDATABASE().connect();

        AuthenticationUtil authenticationUtil = new AuthenticationUtil();
        authenticationUtil.generateAuthenticationSecret();

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8000), 0);

        Logger logger  = Logger.getLogger("http");
        logger.info("Starting http server...");

        AtomicReference<ServerState> serverState = new AtomicReference<>(ServerState.STARTING);

        HttpServer finalHttpServer = httpServer;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Saving settings file");
            try {
                settingsUtil.saveSettings();
            } catch (IOException e) {
                logger.severe("Something went wrong while saving settings, dumping settings for recovery:");
                System.out.println(Constants.INSTANCE.getGson().toJson(s));
                e.printStackTrace();
            }
            logger.info("Shutting down http server...");
            serverState.set(ServerState.STOPPING);
            finalHttpServer.stop(2);
            logger.info("Http server stopped");
        }));

        httpServer.setExecutor(Executors.newCachedThreadPool());

        handlerLoader = new HandlerLoader(httpServer);

        // Register handlers

        handler(Index::new);
        handler(() -> new Status(serverState));
        handler(Token::new);
        handler(User::new);

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
