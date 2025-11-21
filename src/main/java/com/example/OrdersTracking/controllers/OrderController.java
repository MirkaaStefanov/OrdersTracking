package com.example.OrdersTracking.controllers;

import com.example.OrdersTracking.models.Order;
import com.example.OrdersTracking.services.OrderService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/order")
@Data
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/track/{id}/{token}")
    public String trackOrder(@PathVariable Long id, @PathVariable String token, Model model) throws ChangeSetPersister.NotFoundException {
        Order order = orderService.findByIdAndToken(id,token);
        model.addAttribute("order", order);
        return "order/track";
    }

}
