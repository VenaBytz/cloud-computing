package com.autos.zuul.controller;

import com.autos.zuul.service.ProductProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de demostración del proxy con Hystrix.
 *
 * Endpoints disponibles:
 *   GET /proxy/productos          → lista todos (balanceo Ribbon + Circuit Breaker)
 *   GET /proxy/productos/{id}     → un producto (fallback por error)
 *   GET /proxy/productos-lento    → timeout 1 s (fallback por latencia)
 *
 * Nota: el ruteo transparente de Zuul usa /api/productos/**, /api/items/**
 *       sin pasar por este controlador.
 */
@RestController
@RequestMapping("/proxy")
public class ProxyController {

    @Autowired
    private ProductProxyService productProxyService;

    /** Balanceo de carga entre instancias + Circuit Breaker Hystrix */
    @GetMapping("/productos")
    public Object listarProductos() {
        return productProxyService.listarProductos();
    }

    /** Recuperación de error: si product-service falla → fallback */
    @GetMapping("/productos/{id}")
    public Object obtenerProducto(@PathVariable Long id) {
        return productProxyService.obtenerProducto(id);
    }

    /** Recuperación por latencia: endpoint lento (2 s) con timeout Hystrix de 1 s */
    @GetMapping("/productos-lento")
    public Object listarProductosLento() {
        return productProxyService.listarProductosLento();
    }
}
