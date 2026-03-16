package com.autos.item.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private Integer cantidad;
    private Integer iva;
    private Integer total;
    private LocalDateTime fecha;

    public Item() {}

    public Item(Long productId, Integer cantidad, Integer iva, Integer total, LocalDateTime fecha) {
        this.productId = productId;
        this.cantidad = cantidad;
        this.iva = iva;
        this.total = total;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getTotal(){
      return total;
    }

    public void setTotal(Integer total){
      this.total = total;
    }

    public LocalDateTime getFecha(){
      return fecha;
    }

    public void setFecha(LocalDateTime fecha){
      this.fecha = fecha;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getIva() {
        return iva;
    }

    public void setIva(Integer iva) {
        this.iva = iva;
    }
}
