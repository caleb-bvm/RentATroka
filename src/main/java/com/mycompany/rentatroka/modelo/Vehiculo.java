package com.mycompany.rentatroka.modelo;

import java.io.Serializable;
import java.math.BigDecimal;

// Bean de un vehículo disponible para alquiler

public class Vehiculo implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String placa;
    private String marca;
    private String modelo;
    private BigDecimal tarifaDiaria;

    public Vehiculo() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public BigDecimal getTarifaDiaria() {
        return tarifaDiaria;
    }

    public void setTarifaDiaria(BigDecimal tarifaDiaria) {
        this.tarifaDiaria = tarifaDiaria;
    }
}
