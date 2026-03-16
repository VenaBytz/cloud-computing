package com.autos.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.autos.item.dto.ItemDto;
import com.autos.item.service.ItemService;

@RestController
@RequestMapping("/items")
public class ItemController {

  @Autowired
  private ItemService service;

  @GetMapping("/{id}")
  public ItemDto getItemById(@PathVariable Long id) {
      return service.getItemById(id);
  }

  @PostMapping
  public ItemDto createItem(@RequestParam Long productId, @RequestParam Integer cantidad) {
      return service.createItem(productId, cantidad);
  }

  @DeleteMapping("/{id}")
  public void deleteItem(@PathVariable Long id) {
      service.deleteItem(id);
  }

  @DeleteMapping("/rest/{id}")
  public void deleteItemRest(@PathVariable Long id) {
      service.deleteItemRestTemplate(id);
  }
}
