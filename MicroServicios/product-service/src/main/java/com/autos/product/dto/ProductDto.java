package com.autos.product.dto;

public class ProductDto {

    private Long id;
    private String modelo;
    private double precio;
    private String marca;
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
    public String getmodelo() {
        return modelo;
    }

    public void setmodelo(String modelo) {
        this.modelo = modelo;
    }

    public double getprecio() {
        return precio;
    }

    public void setprecio(double precio) {
        this.precio = precio;
    }

    public String getmarca() {
        return marca;
    }

    public void setmarca(String marca) {
        this.marca = marca;
    }

    public String gettemporada() {
        return temporada;
    }

    public void settemporada(String temporada) {
        this.temporada = temporada;
    }
}
