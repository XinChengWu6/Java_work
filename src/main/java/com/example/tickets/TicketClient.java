package com.example.tickets;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Connects to the ticket server and sends local file purchase records.
 */
public class TicketClient {
    private final String serverHost;
    private final int serverPort;
    private final Path localFile;

    public TicketClient(String serverHost, int serverPort, Path localFile) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.localFile = localFile;
    }

    public void sendTickets() throws IOException {
        List<TicketInfo> tickets = loadTickets();
        try (Socket socket = new Socket(serverHost, serverPort);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())) {
            for (TicketInfo ticket : tickets) {
                outputStream.writeObject(ticket);
            }
            outputStream.writeObject("END");
            outputStream.flush();
            System.out.printf("Client %s sent %d records to server.%n", localFile.getFileName(), tickets.size());
        }
    }

    private List<TicketInfo> loadTickets() throws IOException {
        return Files.lines(localFile)
                .filter(line -> !line.isBlank())
                .map(this::parseLine)
                .collect(Collectors.toList());
    }

    private TicketInfo parseLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid ticket line in " + localFile + ": " + line);
        }
        return new TicketInfo(parts[0], parts[1], parts[2], parts[3]);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 3) {
            System.err.println("Usage: java com.example.tickets.TicketClient <file> [host] [port]");
            return;
        }
        Path file = Path.of(args[0]);
        String host = args.length >= 2 ? args[1] : "localhost";
        int port = args.length >= 3 ? Integer.parseInt(args[2]) : TicketServer.DEFAULT_PORT;
        TicketClient client = new TicketClient(host, port, file);
        client.sendTickets();
    }
}
