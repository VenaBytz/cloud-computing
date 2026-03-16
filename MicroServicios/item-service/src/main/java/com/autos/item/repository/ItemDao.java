package com.autos.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.autos.item.entity.Item;

@Repository
public interface ItemDao extends JpaRepository<Item, Long> {
}
