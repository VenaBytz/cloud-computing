package com.autos.item.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import com.autos.item.dto.ProductDto;

@FeignClient(name="product-service", url="http://localhost:8081")
public interface ProductClient {

    @GetMapping("/products/ver/{id}")
    ProductDto getProduct(@PathVariable("id") Long id);
    
    @DeleteMapping("/products/{id}")
    void deleteProduct(@PathVariable("id") Long id);
}
