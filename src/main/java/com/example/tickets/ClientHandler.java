package com.example.tickets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Server-side worker that reads incoming ticket messages from a client.
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final TicketFileStorage fileStorage;
    private final TicketDatabase database;
    private final AtomicInteger processedCount;

    public ClientHandler(Socket socket, TicketFileStorage fileStorage, TicketDatabase database, AtomicInteger processedCount) {
        this.socket = socket;
        this.fileStorage = fileStorage;
        this.database = database;
        this.processedCount = processedCount;
    }

    @Override
    public void run() {
        try (ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
            Object incoming;
            while ((incoming = inputStream.readObject()) != null) {
                if (incoming instanceof String && "END".equals(incoming)) {
                    break;
                }
                if (incoming instanceof TicketInfo ticketInfo) {
                    fileStorage.append(ticketInfo);
                    database.insert(ticketInfo);
                    processedCount.incrementAndGet();
                    System.out.printf("Stored ticket from %s (%s)%n", ticketInfo.getName(), socket.getRemoteSocketAddress());
                } else {
                    System.err.println("Received unknown payload: " + incoming);
                }
            }
        } catch (IOException e) {
            System.err.println("I/O error while handling client " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Invalid object received from client " + socket.getRemoteSocketAddress());
        }
    }
}
