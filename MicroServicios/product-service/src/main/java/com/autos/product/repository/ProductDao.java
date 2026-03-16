package com.autos.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.autos.product.entity.Product;

public interface ProductDao extends JpaRepository<Product, Long> {

}