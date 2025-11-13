package com.example.OrdersTracking.repositories;

import com.example.OrdersTracking.models.User;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {
    public User getUserByUsername(String username);
    public User getUserByEmail(String email);
    List<User> findAll();


}
