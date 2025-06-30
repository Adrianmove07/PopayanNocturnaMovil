// ReservaResponse.java
package com.example.popayan_noc.model;

import java.util.List;

public class ReservaResponse {
    private boolean ok;
    private int total;
    private List<Reserva> reservas;

    // Getters and Setters
    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }
}