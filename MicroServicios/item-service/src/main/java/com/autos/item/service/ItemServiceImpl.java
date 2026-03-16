package com.autos.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.autos.item.client.ProductClient;
import com.autos.item.dto.ItemDto;
import com.autos.item.dto.ProductDto;
import com.autos.item.entity.Item;
import com.autos.item.repository.ItemDao;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductClient productClient;
    @Autowired
    private ItemDao itemDao;              

    @Autowired
    private RestTemplate restTemplate;
    private final String PRODUCT_DELETE_URL = "http://localhost:8081/products/";

    
    @Override
    public ItemDto createItem(Long productId, Integer cantidad) {
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
    }

    @Override
    public ItemDto getItemById(Long id) {
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
}
