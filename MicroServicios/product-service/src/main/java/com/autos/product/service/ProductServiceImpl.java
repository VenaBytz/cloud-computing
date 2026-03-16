package com.autos.product.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autos.product.repository.ProductDao;
import com.autos.product.dto.ProductDto;
import com.autos.product.entity.Product;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDao productDao;

    @Override
    @Transactional
    public List<ProductDto> getProducts() {
        return productDao.findAll()
                          .stream()
                          .map(this::convertToDto)
                          .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDto getProduct(Long id) {
        Product p = productDao.findById(id).orElse(null);
        return p != null ? convertToDto(p) : null;
    }

    @Override
    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product p = convertToEntity(dto);
        Product saved = productDao.save(p);
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product existing = productDao.findById(id).orElse(null);
        if (existing != null) {
            existing.setmodelo(dto.getmodelo());
            existing.setprecio(dto.getprecio());
            Product saved = productDao.save(existing);
            return convertToDto(saved);
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        productDao.deleteById(id);
    }

    private ProductDto convertToDto(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setmodelo(p.getmodelo());
        dto.setprecio(p.getprecio());
        dto.setmarca(p.getmarca());
        dto.settemporada(p.gettemporada());
        return dto;
    }

    private Product convertToEntity(ProductDto dto) {
        Product p = new Product();
        p.setmodelo(dto.getmodelo());
        p.setprecio(dto.getprecio());
        p.setmarca(dto.getmarca());
        p.settemporada(dto.gettemporada());
        return p;
    }
}
