package com.example.tickets;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Convenience launcher that starts four clients concurrently to demonstrate the system.
 */
public class SampleClientLauncher {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        List<Path> clientFiles = List.of(
                Path.of("client_data/client1.txt"),
                Path.of("client_data/client2.txt"),
                Path.of("client_data/client3.txt"),
                Path.of("client_data/client4.txt")
        );
        for (Path file : clientFiles) {
            executorService.submit(() -> {
                try {
                    new TicketClient("localhost", TicketServer.DEFAULT_PORT, file).sendTickets();
                } catch (IOException e) {
                    System.err.println("Failed to send tickets for file " + file + ": " + e.getMessage());
                }
            });
        }
        executorService.shutdown();
    }
}
