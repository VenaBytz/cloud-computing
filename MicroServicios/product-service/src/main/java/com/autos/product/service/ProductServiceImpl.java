package com.autos.product.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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
    public ProductDto creaProducto(ProductDto dto) {
        Product p = convertToEntity(dto);
        Product saved = productDao.save(p);
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public ProductDto modificaProducto(Long id, ProductDto dto) {
        Product existing = productDao.findById(id).orElse(null);
        if (existing != null) {
            existing.setModelo(dto.getModelo());
            existing.setPrecio(dto.getPrecio());
            existing.setMarca(dto.getMarca());
            existing.setTemporada(dto.getTemporada());
            Product saved = productDao.save(existing);
            return convertToDto(saved);
        }
        return null;
    }

  	@Override
	  @Transactional(readOnly = false)
    public String eliminaProducto(Long id) {
      Product p = productDao.findById(id).orElse(null);
      if (p != null) {
	      productDao.delete(p);
        return "Producto eliminado";
      } else {
        return "Producto no encontrado";
      }
    }

  	@Override
  	@Transactional(readOnly = true)
    public Long cuentaProductos() {
    	return productDao.count();
    }

    private ProductDto convertToDto(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setModelo(p.getModelo());
        dto.setPrecio(p.getPrecio());
        dto.setMarca(p.getMarca());
        dto.setTemporada(p.getTemporada());
        return dto;
    }

    private Product convertToEntity(ProductDto dto) {
        Product p = new Product();
        p.setModelo(dto.getModelo());
        p.setPrecio(dto.getPrecio());
        p.setMarca(dto.getMarca());
        p.setTemporada(dto.getTemporada());
        return p;
    }
}
