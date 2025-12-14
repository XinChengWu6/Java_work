package com.example.tickets;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple server that receives ticket purchase records from multiple clients.
 */
public class TicketServer {
    public static final int DEFAULT_PORT = 5000;
    public static final int MAX_CLIENTS = 4;
    public static final Path DEFAULT_DATA_DIR = Path.of("server_storage");

    private final int port;
    private final ThreadPoolExecutor executorService;
    private final TicketFileStorage fileStorage;
    private final TicketDatabase ticketDatabase;

    public TicketServer(int port) {
        this.port = port;
        this.executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_CLIENTS);
        this.fileStorage = new TicketFileStorage(DEFAULT_DATA_DIR.resolve("tickets.log"));
        this.ticketDatabase = new TicketDatabase(DEFAULT_DATA_DIR.resolve("tickets.db"));
        this.ticketDatabase.initialize();
    }

    public void start() throws IOException {
        AtomicInteger processedCount = new AtomicInteger();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("Server started on port %d at %s%n", port, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
            while (true) {
                Socket clientSocket = serverSocket.accept();
                if (executorService.getActiveCount() >= MAX_CLIENTS) {
                    System.err.println("Maximum client connections reached. Rejecting client " + clientSocket.getRemoteSocketAddress());
                    clientSocket.close();
                    continue;
                }
                System.out.println("Accepted connection from " + clientSocket.getRemoteSocketAddress());
                executorService.submit(new ClientHandler(clientSocket, fileStorage, ticketDatabase, processedCount));
            }
        }
    }

    public TicketFileStorage getFileStorage() {
        return fileStorage;
    }

    public TicketDatabase getTicketDatabase() {
        return ticketDatabase;
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        TicketServer server = new TicketServer(port);
        server.start();
    }
}
