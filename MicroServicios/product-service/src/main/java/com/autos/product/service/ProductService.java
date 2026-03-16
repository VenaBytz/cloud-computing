package com.autos.product.service;

import com.autos.product.dto.ProductDto;
import java.util.List;

public interface ProductService {

    List<ProductDto> getProducts();

    ProductDto getProduct(Long id);

    ProductDto createProduct(ProductDto product);

    ProductDto updateProduct(Long id, ProductDto product);

    void deleteProduct(Long id);

}
