package com.example.OrdersTracking.repositories;

import com.example.OrdersTracking.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndAccessToken(Long id, String token);
}
