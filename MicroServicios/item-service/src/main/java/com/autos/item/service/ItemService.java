package com.autos.item.service;

import com.autos.item.dto.ItemDto;

public interface ItemService {

    void deleteItem(Long itemId);
    void deleteItemRestTemplate(Long itemId);
    ItemDto createItem(Long productId, Integer cantidad);
    ItemDto getItemById(Long id);
}
