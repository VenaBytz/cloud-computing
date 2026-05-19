package com.autos.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ProductDto {
    
    @Positive
    private Long id;
    
    @NotNull
    private String modelo;

    @Positive
    private double precio;

    @NotNull
    private String marca;

    @NotNull
    private String temporada;

    public ProductDto() {}

    public ProductDto(Long id,String modelo, double precio, String marca, String temporada){
        this.id = id;
        this.modelo = modelo;
        this.precio = precio;
        this.marca = marca;
        this.temporada = temporada;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getTemporada() {
        return temporada;
    }

    public void setTemporada(String temporada) {
        this.temporada = temporada;
    }
}
