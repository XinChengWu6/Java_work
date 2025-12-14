package com.example.tickets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Writes ticket information to a text file and can read it back.
 */
public class TicketFileStorage {
    private final Path filePath;

    public TicketFileStorage(Path filePath) {
        this.filePath = filePath;
    }

    public synchronized void append(TicketInfo ticketInfo) {
        try {
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, ticketInfo + System.lineSeparator(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write ticket to file", e);
        }
    }

    public List<TicketInfo> readAll() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        try {
            return Files.lines(filePath)
                    .filter(line -> !line.isBlank())
                    .map(this::fromLine)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read tickets from file", e);
        }
    }

    private TicketInfo fromLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid ticket record: " + line);
        }
        return new TicketInfo(parts[0], parts[1], parts[2], parts[3]);
    }
}
