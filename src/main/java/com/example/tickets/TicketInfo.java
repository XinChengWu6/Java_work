package com.example.tickets;

import java.io.Serializable;

/**
 * Simple value object describing a ticket purchase request.
 */
public class TicketInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String idNumber;
    private final String fromCity;
    private final String toCity;

    public TicketInfo(String name, String idNumber, String fromCity, String toCity) {
        this.name = name;
        this.idNumber = idNumber;
        this.fromCity = fromCity;
        this.toCity = toCity;
    }

    public String getName() {
        return name;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public String getFromCity() {
        return fromCity;
    }

    public String getToCity() {
        return toCity;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s", name, idNumber, fromCity, toCity);
    }
}
