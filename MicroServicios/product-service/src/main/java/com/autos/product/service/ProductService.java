package com.autos.product.service;

import com.autos.product.dto.ProductDto;
import java.util.List;

public interface ProductService {

    List<ProductDto> getProducts();

    ProductDto getProduct(Long id);

    ProductDto creaProducto(ProductDto product);

    ProductDto modificaProducto(Long id, ProductDto product);

    String eliminaProducto(Long id);
    
    Long cuentaProductos();
}
