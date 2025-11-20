package com.example.OrdersTracking.controllers;

import com.example.OrdersTracking.models.Order;
import com.example.OrdersTracking.services.OrderService;
import com.example.OrdersTracking.services.SseService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Controller
@RequestMapping("/kitchen")
@Data
@AllArgsConstructor
public class KitchenController {

    private final OrderService orderService;
    private final SseService sseService;

    @GetMapping
    public String getOrders(Model model){
        List<Order> orders = orderService.ordersForKitchen();
        model.addAttribute("orders", orders);
        return "kitchen/orders";
    }

    @GetMapping("/stream")
    public SseEmitter stream() {
        return sseService.subscribe();
    }

}
