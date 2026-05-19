package com.autos.product.service;

import com.autos.product.dto.ProductDto;
import com.autos.product.entity.Product;
import com.autos.product.repository.ProductDao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceImplTest {

    @Mock
    private ProductDao repository;

    @InjectMocks
    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetProducts() {

        Product p1 = new Product(
                1L,
                "Sedan X1",
                20000,
                "Toyota",
                "Verano"
        );

        Mockito.when(repository.findAll())
                .thenReturn(List.of(p1));

        List<ProductDto> productos =
                service.getProducts();

        assertEquals(1, productos.size());

        assertEquals(
                "Sedan X1",
                productos.get(0).getModelo()
        );
    }

    @Test
    void testGetProduct() {

        Product p1 = new Product(
                1L,
                "Sedan X1",
                20000,
                "Toyota",
                "Verano"
        );

        Mockito.when(repository.findById(1L))
                .thenReturn(Optional.of(p1));

        ProductDto producto =
                service.getProduct(1L);

        assertNotNull(producto);

        assertEquals(
                "Toyota",
                producto.getMarca()
        );
    }

    @Test
    void testCuentaProductos() {

        Mockito.when(repository.count())
                .thenReturn(5L);

        Long total =
                service.cuentaProductos();

        assertEquals(5L, total);
    }

    @Test
    void testEliminarProducto() {

        Mockito.doNothing()
                .when(repository)
                .deleteById(1L);

        String resultado =
                service.eliminaProducto(1L);

        assertNotNull(resultado);
    }
}
