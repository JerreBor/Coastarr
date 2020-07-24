package nl.jerodeveloper.coastarr.api.filters;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.function.Supplier;

public class Tracing extends Filter {

    private final Supplier<String> nextRequestId;

    public Tracing(Supplier<String> nextRequestId) {
        this.nextRequestId = nextRequestId;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String requestId = exchange.getRequestHeaders().getFirst("X-Request-Id");
        if (requestId == null) requestId = nextRequestId.get();
        exchange.setAttribute("requestId", requestId);
        exchange.getResponseHeaders().add("X-Request-Id", requestId);
        chain.doFilter(exchange);
    }

    @Override
    public String description() {
        return "tracing";
    }
}
