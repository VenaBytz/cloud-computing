package com.autos.zuul.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio proxy que demuestra el uso de @HystrixCommand para:
 * 1. Recuperación de errores (fallback cuando el servicio falla)
 * 2. Protección contra latencia (fallback cuando se excede 1 segundo)
 *
 * Ribbon balancea automáticamente entre instancias de product-service
 * registradas en Eureka al usar el nombre de servicio en la URL.
 */
@Service
public class ProductProxyService {

    private static final Logger logger = LoggerFactory.getLogger(ProductProxyService.class);

    @Autowired
    private RestTemplate restTemplate; // @LoadBalanced: Ribbon balancea entre instancias

    // ----------------------------------------------------------------
    // FUNCIONALIDAD 1: Balanceo de carga con Hystrix
    // Ribbon distribuye las peticiones entre product-service y
    // product-service-2 registradas en Eureka (round-robin).
    // Hystrix abre el circuito si el 50% de llamadas fallan.
    // ----------------------------------------------------------------
    @HystrixCommand(
        fallbackMethod = "listarProductosFallback",
        commandProperties = {
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "5"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50"),
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000")
        }
    )
    public Object listarProductos() {
        logger.info("[HYSTRIX] Llamando a product-service/products/list con balanceo Ribbon");
        return restTemplate.getForObject("http://product-service/products/list", Object.class);
    }

    // ----------------------------------------------------------------
    // FUNCIONALIDAD 2: Recuperación de errores con @HystrixCommand
    // Cuando product-service lanza excepción (servicio caído, 404, etc.),
    // Hystrix intercepta y llama a obtenerProductoFallback().
    // ----------------------------------------------------------------
    @HystrixCommand(fallbackMethod = "obtenerProductoFallback")
    public Object obtenerProducto(Long id) {
        logger.info("[HYSTRIX] Llamando a product-service/products/ver/{}", id);
        return restTemplate.getForObject(
                "http://product-service/products/ver/" + id, Object.class);
    }

    // ----------------------------------------------------------------
    // FUNCIONALIDAD 3: Recuperación por latencia (timeout > 1 segundo)
    // Si product-service tarda más de 1000 ms, Hystrix cancela la
    // llamada y ejecuta listarLentoFallback() inmediatamente.
    // ----------------------------------------------------------------
    @HystrixCommand(
        fallbackMethod = "listarLentoFallback",
        commandProperties = {
            @HystrixProperty(
                name  = "execution.isolation.thread.timeoutInMilliseconds",
                value = "1000"   // 1 segundo máximo
            )
        }
    )
    public Object listarProductosLento() {
        logger.info("[HYSTRIX] Llamando endpoint lento (demora 2 s). Timeout configurado: 1 s");
        // /products/slow duerme 2 segundos → dispara el timeout de 1 s
        return restTemplate.getForObject("http://product-service/products/slow", Object.class);
    }

    // ================================================================
    // MÉTODOS FALLBACK
    // ================================================================

    public Object listarProductosFallback(Throwable e) {
        logger.warn("[FALLBACK] listarProductos falló: {}", e.getMessage());
        Map<String, Object> resp = new HashMap<>();
        resp.put("fallback", true);
        resp.put("productos", Collections.emptyList());
        resp.put("mensaje", "product-service no disponible. Lista vacía retornada por @HystrixCommand.");
        resp.put("origen", "@HystrixCommand fallbackMethod");
        return resp;
    }

    public Object obtenerProductoFallback(Long id, Throwable e) {
        logger.warn("[FALLBACK] obtenerProducto({}) falló: {}", id, e.getMessage());
        Map<String, Object> resp = new HashMap<>();
        resp.put("fallback", true);
        resp.put("id", id);
        resp.put("modelo", "PRODUCTO NO DISPONIBLE");
        resp.put("marca", "N/A");
        resp.put("precio", 0.0);
        resp.put("mensaje", "product-service falló. Datos alternativos retornados por @HystrixCommand.");
        resp.put("origen", "@HystrixCommand fallbackMethod");
        return resp;
    }

    public Object listarLentoFallback(Throwable e) {
        logger.warn("[FALLBACK-LATENCIA] Timeout superado (>1 s): {}", e.getMessage());
        Map<String, Object> resp = new HashMap<>();
        resp.put("fallback", true);
        resp.put("productos", Collections.emptyList());
        resp.put("mensaje", "El servicio tardó más de 1 segundo. Método alternativo activado por LATENCIA.");
        resp.put("timeout_ms", 1000);
        resp.put("origen", "@HystrixCommand timeoutInMilliseconds=1000");
        return resp;
    }
}
