package com.autos.product.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.autos.product.dto.ProductDto;
import com.autos.product.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Value("${server.port}")
    private int port;

	@Autowired
    private ProductService service;

    @GetMapping("/list")
    public List<ProductDto> getProducts() {
        return service.getProducts();
    }

    @GetMapping("/ver/{id}")
    public ProductDto detalle(@PathVariable Long id) {
      return service.getProduct(id);
    }
    
    @PostMapping
    public ProductDto createProduct(@RequestBody ProductDto product) {
        return service.createProduct(product);
    }

    @PutMapping("/{id}")
    public ProductDto updateProduct(@PathVariable Long id, @RequestBody ProductDto product) {
        return service.updateProduct(id, product);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        service.deleteProduct(id);
    }

    /**
     * Endpoint lento para demostrar la recuperación por latencia de Hystrix.
     * Duerme 2 segundos; el timeout configurado en Zuul es 1 segundo,
     * por lo que Zuul activará el fallback antes de recibir respuesta.
     */
    @GetMapping("/slow")
    public ResponseEntity<List<ProductDto>> listarSlow() throws InterruptedException {
        logger.info("[SLOW] Endpoint lento invocado en puerto {}. Esperando 2 segundos...", port);
        Thread.sleep(2000);
        return ResponseEntity.ok(service.getProducts());
    }

    /**
     * Informa en qué instancia (puerto) se está ejecutando esta petición.
     * Permite verificar el balanceo de carga entre product-service y product-service-2.
     */
    @GetMapping("/instance-info")
    public ResponseEntity<Map<String, Object>> instanceInfo() throws java.net.UnknownHostException {
        Map<String, Object> info = new HashMap<>();
        info.put("servicio", "product-service");
        info.put("puerto", port);
        info.put("host", java.net.InetAddress.getLocalHost().getHostName());
        info.put("ip", java.net.InetAddress.getLocalHost().getHostAddress());
        info.put("mensaje", "Respondiendo desde la instancia en puerto " + port);
        return ResponseEntity.ok(info);
    }
}
