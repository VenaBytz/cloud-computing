package com.autos.product.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.autos.product.dto.ProductDto;
import com.autos.product.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {
	
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
}
