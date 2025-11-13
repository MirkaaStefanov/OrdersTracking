package com.example.OrdersTracking.repositories;

import com.example.OrdersTracking.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.nio.file.LinkOption;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
