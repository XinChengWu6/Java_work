package com.example.tickets;

import java.io.IOException;
import java.util.List;

/**
 * Utility to read and print server-side records from the flat file and database.
 */
public class ServerDataViewer {
    public static void main(String[] args) throws IOException {
        TicketFileStorage fileStorage = new TicketFileStorage(TicketServer.DEFAULT_DATA_DIR.resolve("tickets.log"));
        TicketDatabase database = new TicketDatabase(TicketServer.DEFAULT_DATA_DIR.resolve("tickets.db"));
        database.initialize();

        System.out.println("==== Records from server file ====");
        printTickets(fileStorage.readAll());

        System.out.println("==== Records from server database ====");
        printTickets(database.readAll());
    }

    private static void printTickets(List<TicketInfo> tickets) {
        if (tickets.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        for (TicketInfo ticket : tickets) {
            System.out.printf("Name: %s, ID: %s, From: %s, To: %s%n",
                    ticket.getName(), ticket.getIdNumber(), ticket.getFromCity(), ticket.getToCity());
        }
    }
}
