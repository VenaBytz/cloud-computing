package com.autos.item.service;
import java.util.concurrent.CompletableFuture; 
import com.autos.item.dto.ItemDto;

public interface ItemService {
    void deleteItem(Long itemId);
    void deleteItemRestTemplate(Long itemId);
    CompletableFuture<ItemDto> createItem(Long productId, Integer cantidad);
    CompletableFuture<ItemDto> getItemById(Long id);
}
