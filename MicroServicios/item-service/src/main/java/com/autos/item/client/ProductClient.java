package com.autos.item.client;
import java.util.List; 
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import com.autos.item.dto.ProductDto;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/ver/{id}")
    ProductDto getProduct(@PathVariable("id") Long id);

    @GetMapping("/products/slow")
    ProductDto getProductSlow();

    @GetMapping("/products/list")
    List<ProductDto> findAll();

    @DeleteMapping("/products/eliminar/{id}")
    void deleteProduct(@PathVariable("id") Long id);
}
