package com.example.OrdersTracking.controllers;

import com.example.OrdersTracking.models.Product;
import com.example.OrdersTracking.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    @GetMapping("/")
    public String home() {
    return "redirect:/menu";
    }

    @GetMapping("/menu")
    public String menu(Model model) {
        List<Product> menu = productService.getMenuForOrder();
        model.addAttribute("activePage", "menu");
        model.addAttribute("menu", menu);
        return "menu";
    }
}
