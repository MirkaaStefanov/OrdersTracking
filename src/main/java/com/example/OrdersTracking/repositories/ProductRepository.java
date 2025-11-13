package com.example.OrdersTracking.repositories;

import com.example.OrdersTracking.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.nio.file.LinkOption;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

}
