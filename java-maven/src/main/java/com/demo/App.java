package com.demo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class App {

    private static final int DEFAULT_PORT = 8050;
    private static final GreetingService greetingService =
            new GreetingService();

    public static void main(String[] args) throws IOException {
        int port = getPort();

        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", port),
                0
        );

        server.createContext("/", App::handleHomeRequest);
        server.createContext("/greeting", App::handleGreetingRequest);
        server.createContext("/health", App::handleHealthRequest);

        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();

        System.out.println("Application started successfully.");
        System.out.println("Listening on port " + port);
        System.out.println("Home: http://localhost:" + port + "/");
        System.out.println(
                "Greeting: http://localhost:"
                        + port
                        + "/greeting?name=Abdelrahman"
        );
        System.out.println(
                "Health: http://localhost:"
                        + port
                        + "/health"
        );
    }

    private static void handleHomeRequest(HttpExchange exchange)
            throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(
                    exchange,
                    405,
                    "Method Not Allowed",
                    "text/plain"
            );
            return;
        }

        String response = """
                Demo Java application is running.

                Available endpoints:
                GET /
                GET /greeting
                GET /greeting?name=Abdelrahman
                GET /health
                """;

        sendResponse(exchange, 200, response, "text/plain");
    }

    private static void handleGreetingRequest(HttpExchange exchange)
            throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(
                    exchange,
                    405,
                    "Method Not Allowed",
                    "text/plain"
            );
            return;
        }

        Map<String, String> parameters = parseQuery(
                exchange.getRequestURI().getRawQuery()
        );

        String name = parameters.get("name");
        String greeting = greetingService.createGreeting(name);

        String response = """
                {
                  "message": "%s"
                }
                """.formatted(escapeJson(greeting));

        sendResponse(
                exchange,
                200,
                response,
                "application/json"
        );
    }

    private static void handleHealthRequest(HttpExchange exchange)
            throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(
                    exchange,
                    405,
                    "Method Not Allowed",
                    "text/plain"
            );
            return;
        }

        String response = """
                {
                  "status": "UP"
                }
                """;

        sendResponse(
                exchange,
                200,
                response,
                "application/json"
        );
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> parameters = new HashMap<>();

        if (query == null || query.isBlank()) {
            return parameters;
        }

        for (String parameter : query.split("&")) {
            String[] parts = parameter.split("=", 2);

            String key = URLDecoder.decode(
                    parts[0],
                    StandardCharsets.UTF_8
            );

            String value = parts.length == 2
                    ? URLDecoder.decode(
                            parts[1],
                            StandardCharsets.UTF_8
                    )
                    : "";

            parameters.put(key, value);
        }

        return parameters;
    }

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response,
            String contentType
    ) throws IOException {

        byte[] responseBytes = response.getBytes(
                StandardCharsets.UTF_8
        );

        exchange.getResponseHeaders().set(
                "Content-Type",
                contentType + "; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
                statusCode,
                responseBytes.length
        );

        try (OutputStream outputStream =
                     exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }

    private static int getPort() {
        String configuredPort = System.getenv("PORT");

        if (configuredPort == null || configuredPort.isBlank()) {
            return DEFAULT_PORT;
        }

        try {
            return Integer.parseInt(configuredPort);
        } catch (NumberFormatException exception) {
            System.err.println(
                    "Invalid PORT value. Using port "
                            + DEFAULT_PORT
            );

            return DEFAULT_PORT;
        }
    }

    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}