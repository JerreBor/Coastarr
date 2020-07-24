package nl.jerodeveloper.Coastarr.api.filters;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.logging.Logger;

public class Logging extends Filter {

    private final Logger logger;

    public Logging(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        try {
            chain.doFilter(exchange);
        } finally {
            Object possibleRequestId = exchange.getAttribute("requestId");
            String requestId = possibleRequestId instanceof String ? (String) possibleRequestId : "Unknown";
            logger.info(String.format("%s %s %s %s %s",
                                        requestId,
                                        exchange.getRequestMethod(),
                                        exchange.getRequestURI().getPath(),
                                        exchange.getRemoteAddress().getHostString(),
                                        exchange.getRequestHeaders().getFirst("User-Agent")
                    ));
        }
    }

    @Override
    public String description() {
        return "logging";
    }
}
