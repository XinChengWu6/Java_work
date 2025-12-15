package com.example.tickets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Minimal HTTP server that exposes ticket data and a static web UI.
 */
public class TicketWebServer {
    private static final int DEFAULT_HTTP_PORT = 8080;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final int port;
    private final HttpServer server;
    private final TicketDatabase database;
    private final TicketFileStorage fileStorage;

    public TicketWebServer(int port) {
        this.port = port;
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create HTTP server", e);
        }
        this.database = new TicketDatabase(TicketServer.DEFAULT_DATA_DIR.resolve("tickets.db"));
        this.database.initialize();
        this.fileStorage = new TicketFileStorage(TicketServer.DEFAULT_DATA_DIR.resolve("tickets.log"));
        registerHandlers();
    }

    private void registerHandlers() {
        server.createContext("/api/tickets", new TicketApiHandler());
        server.createContext("/", new StaticFileHandler());
    }

    public void start() {
        server.start();
        System.out.printf("Web UI started on http://localhost:%d%n", port);
    }

    public void stop(int delaySeconds) {
        server.stop(delaySeconds);
    }

    private class TicketApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    handleGet(exchange);
                } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    handlePost(exchange);
                } else {
                    sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "Server error: " + e.getMessage(), "text/plain");
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            List<TicketInfo> tickets = database.readAll();
            String json = GSON.toJson(Map.of("tickets", tickets));
            sendResponse(exchange, 200, json, "application/json");
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            TicketPayload payload = GSON.fromJson(body, TicketPayload.class);
            if (payload == null || !payload.isValid()) {
                sendResponse(exchange, 400, "Invalid request body", "text/plain");
                return;
            }
            TicketInfo ticketInfo = payload.toTicketInfo();
            fileStorage.append(ticketInfo);
            database.insert(ticketInfo);
            sendResponse(exchange, 201, GSON.toJson(Map.of("status", "created")), "application/json");
        }
    }

    private class StaticFileHandler implements HttpHandler {
        private static final String RESOURCE_ROOT = "/public";

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path)) {
                path = "/index.html";
            }
            String resourcePath = RESOURCE_ROOT + path;
            InputStream resourceStream = TicketWebServer.class.getResourceAsStream(resourcePath);
            if (resourceStream == null) {
                sendResponse(exchange, 404, "Not Found", "text/plain");
                return;
            }
            byte[] content = resourceStream.readAllBytes();
            Headers headers = exchange.getResponseHeaders();
            headers.add("Content-Type", contentTypeFor(path));
            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content);
            }
        }

        private String contentTypeFor(String path) {
            if (path.endsWith(".html")) {
                return "text/html; charset=UTF-8";
            }
            if (path.endsWith(".js")) {
                return "application/javascript; charset=UTF-8";
            }
            if (path.endsWith(".css")) {
                return "text/css; charset=UTF-8";
            }
            return "application/octet-stream";
        }
    }

    private static class TicketPayload {
        private String name;
        private String idNumber;
        private String fromCity;
        private String toCity;

        public boolean isValid() {
            return isNonBlank(name) && isNonBlank(idNumber) && isNonBlank(fromCity) && isNonBlank(toCity);
        }

        public TicketInfo toTicketInfo() {
            return new TicketInfo(name.trim(), idNumber.trim(), fromCity.trim(), toCity.trim());
        }

        private boolean isNonBlank(String value) {
            return value != null && !value.trim().isEmpty();
        }
    }

    public static void main(String[] args) {
        int httpPort;
        if (args.length >= 1) {
            httpPort = Integer.parseInt(args[0]);
        } else {
            String envPort = System.getenv("PORT");
            httpPort = envPort != null ? Integer.parseInt(envPort) : DEFAULT_HTTP_PORT;
        }
        TicketWebServer webServer = new TicketWebServer(httpPort);
        webServer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> webServer.stop(0)));
    }
}
