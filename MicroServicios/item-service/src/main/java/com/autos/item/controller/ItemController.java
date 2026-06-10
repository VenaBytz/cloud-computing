package com.autos.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.autos.item.dto.ItemDto;
import com.autos.item.service.ItemService;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemService service;

    @GetMapping("/{id}")
    public ItemDto getItemById(@PathVariable Long id) throws Exception {
        return service.getItemById(id).get();
    }

    @PostMapping
    public ItemDto createItem(@RequestParam Long productId, @RequestParam Integer cantidad) throws Exception {
        return service.createItem(productId, cantidad).get();
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable Long id) {
        service.deleteItem(id);
    }

    @DeleteMapping("/rest/{id}")
    public void deleteItemRest(@PathVariable Long id) {
        service.deleteItemRestTemplate(id);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleFallback(IllegalStateException e) {
        return ResponseEntity.status(503).body(e.getMessage());
    }
}
