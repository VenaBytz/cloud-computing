package com.autos.product.controller;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.autos.product.dto.ProductDto;
import com.autos.product.service.ProductService;
import org.springframework.stereotype.Controller;

@Controller
@RequestMapping("/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Value("${server.port}")
    private int port;

  	@Autowired
    private ProductService service;

    @GetMapping("/list")
    @ResponseBody
    public List<ProductDto> getProducts() {
        return service.getProducts();
    }

    /**
     * Endpoint que lanza excepción controlada para demostrar @CircuitBreaker en item-service.
     * Simula un fallo parcial sin caer todo el servicio.
     */
    @GetMapping("/ver/{id}")
    @ResponseBody
    public ProductDto detalle(@PathVariable Long id) {
      logger.info("Fallo controlado para id: {}", id);
      throw new IllegalStateException("Producto no encontrado!");
    }

    @PostMapping("/crear")
    @ResponseBody
    public ProductDto CreaProductDto( @Validated @RequestBody ProductDto producto ) {
  	return service.creaProducto(producto);
    }   
 
    @PostMapping("/modificar/{id}")
    @ResponseBody
    public ProductDto modificaProductDto(
      @PathVariable Long id,
      @Validated @RequestBody ProductDto producto) {

      return service.modificaProducto(id, producto);
    }	
  	@DeleteMapping("/eliminar/{id}")
    @ResponseBody
  	public String EliminaProductDto(@PathVariable Long id) {
  		return service.eliminaProducto(id);
  	}
	
	  @GetMapping("/contar")
    @ResponseBody
  	public Long CuentaProductDtos() {
  		return service.cuentaProductos();
  	}

    @GetMapping("/modelos")
    public String listar(Model model) {
      model.addAttribute(
        "modelos",
        service.getProducts()
      );
      return "modelos";
    }
    
    /**
     * Endpoint lento para demostrar @TimeLimiter en item-service.
     * Duerme 5s; el timeout configurado en Resilience4J es 2s,
     * por lo que TimeLimiter cancela la llamada y ejecuta el fallback.
     */
    @GetMapping("/slow")
    @ResponseBody
    public ResponseEntity<List<ProductDto>> listarSlow() throws InterruptedException {
        logger.info("Endpoint lento invocado en puerto {}. Esperando 5 segundos...", port);
        TimeUnit.SECONDS.sleep(5L);
        return ResponseEntity.ok(service.getProducts());
    }

    /**
     * Informa en qué instancia (puerto) se está ejecutando esta petición.
     * Permite verificar el balanceo de carga entre product-service y product-service-2.
     */
    @GetMapping("/instance-info")
    @ResponseBody
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
