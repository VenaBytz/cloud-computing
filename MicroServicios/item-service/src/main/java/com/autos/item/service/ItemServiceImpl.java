package com.autos.item.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.autos.item.client.ProductClient;
import com.autos.item.dto.ItemDto;
import com.autos.item.dto.ProductDto;
import com.autos.item.entity.Item;
import com.autos.item.repository.ItemDao;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ItemServiceImpl implements ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemServiceImpl.class);

    @Autowired
    private ProductClient productClient;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private RestTemplate restTemplate;

    private final String PRODUCT_DELETE_URL = "http://product-service:8081/products/";

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackCreateItem")
    public CompletableFuture<ItemDto> createItem(Long productId, Integer cantidad) {
        return CompletableFuture.supplyAsync(() -> {

            ProductDto product = productClient.getProduct(productId);

            Integer iva = (int) (product.getPrecio() * 0.16);
            Integer total = (int) ((product.getPrecio() + iva) * cantidad);

            Item item = new Item();
            item.setProductId(productId);
            item.setCantidad(cantidad);
            item.setIva(iva);
            item.setTotal(total);
            item.setFecha(LocalDateTime.now());

            itemDao.save(item);

            ItemDto dto = new ItemDto();
            dto.setId(item.getId());
            dto.setProductId(productId);
            dto.setCantidad(cantidad);
            dto.setIva(iva);
            dto.setTotal(total);
            dto.setFecha(item.getFecha());
            return dto;
        });
    }

    public CompletableFuture<ItemDto> fallbackCreateItem(Long productId, Integer cantidad, Exception e) {
        logger.error("[CircuitBreaker] Falla creando el item — productId: {}, causa: {}", productId, e.getMessage());
        throw new IllegalStateException("Producto no encontrado!");
    }

    @Override
    @TimeLimiter(name = "productService", fallbackMethod = "fallbackGetItem")
    public CompletableFuture<ItemDto> getItemById(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(5L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Item item = itemDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

            ProductDto product = productClient.getProduct(item.getProductId());

            ItemDto dto = new ItemDto();
            dto.setId(item.getId());
            dto.setProductId(item.getProductId());
            dto.setCantidad(item.getCantidad());
            dto.setIva(item.getIva());
            dto.setTotal(item.getTotal());
            dto.setFecha(item.getFecha());

            return dto;
        });
    }

    public CompletableFuture<ItemDto> fallbackGetItem(Long id, Exception e) {
        logger.error("[TimeLimiter] Timeout obteniendo el item — id: {}, causa: {}", id, e.getMessage());
        throw new IllegalStateException("Servicio de productos no disponible, intente más tarde.");
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        Item item = itemDao.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        Long productId = item.getProductId();
        itemDao.deleteById(itemId);
        productClient.deleteProduct(productId);
    }

    @Override
    public void deleteItemRestTemplate(Long itemId) {
        Item item = itemDao.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        Long productId = item.getProductId();
        itemDao.deleteById(itemId);
        restTemplate.delete(PRODUCT_DELETE_URL + productId);
    }

    public CompletableFuture<List<ProductDto>> getProducts() {
        return CompletableFuture.supplyAsync(() -> productClient.findAll());
    }
}
