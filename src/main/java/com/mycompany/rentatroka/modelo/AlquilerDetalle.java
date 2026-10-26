package com.mycompany.rentatroka.modelo;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

// Una fila del listado: el alquiler + los datos que trae el JOIN con clientes y vehiculos.
// dias y total los calcula MySQL en la misma consulta.

public class AlquilerDetalle extends Alquiler {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String clienteNombre;
    private String placa;
    private String marca;
    private String modelo;
    private int dias;
    private BigDecimal total;

    public AlquilerDetalle() {
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getDias() {
        return dias;
    }

    public void setDias(int dias) {
        this.dias = dias;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    // Fechas ya formateadas para mostrarlas en la tabla (dd/MM/yyyy)
    public String getInicioTexto() {
        return getFechaInicio() == null ? "" : getFechaInicio().format(FORMATO);
    }

    public String getFinTexto() {
        return getFechaFin() == null ? "" : getFechaFin().format(FORMATO);
    }
}