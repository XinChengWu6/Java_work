package com.example.tickets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persisting ticket information to an embedded SQLite database.
 */
public class TicketDatabase {
    private final Path databasePath;

    public TicketDatabase(Path databasePath) {
        this.databasePath = databasePath;
    }

    public void initialize() {
        try {
            Files.createDirectories(databasePath.getParent());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create database directory", e);
        }

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS tickets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "id_number TEXT NOT NULL," +
                    "from_city TEXT NOT NULL," +
                    "to_city TEXT NOT NULL"
                    + ")");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public void insert(TicketInfo info) {
        String sql = "INSERT INTO tickets (name, id_number, from_city, to_city) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, info.getName());
            statement.setString(2, info.getIdNumber());
            statement.setString(3, info.getFromCity());
            statement.setString(4, info.getToCity());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert ticket", e);
        }
    }

    public List<TicketInfo> readAll() {
        List<TicketInfo> tickets = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT name, id_number, from_city, to_city FROM tickets");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                tickets.add(new TicketInfo(
                        resultSet.getString("name"),
                        resultSet.getString("id_number"),
                        resultSet.getString("from_city"),
                        resultSet.getString("to_city")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to read tickets from database", e);
        }
        return tickets;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + databasePath.toString());
    }
}
