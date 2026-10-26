package com.mycompany.rentatroka.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

// Bean de un alquiler: un cliente, un vehículo y un rango de fechas.
// El total no se guarda: se calcula con los días y la tarifa aplicada.

public class Alquiler implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int clienteId;
    private int vehiculoId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal tarifaDiariaAplicada;

    public Alquiler() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public int getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(int vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public BigDecimal getTarifaDiariaAplicada() {
        return tarifaDiariaAplicada;
    }

    public void setTarifaDiariaAplicada(BigDecimal tarifaDiariaAplicada) {
        this.tarifaDiariaAplicada = tarifaDiariaAplicada;
    }
}