package com.example.OrdersTracking.controllers;

import com.example.OrdersTracking.enums.MenuCategory;
import com.example.OrdersTracking.models.Product;
import com.example.OrdersTracking.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
    public String home(Authentication authentication) {
        // 1. Check if user is authenticated
        if (authentication != null && authentication.isAuthenticated()) {

            // 2. Check for ADMIN role
            // Note: Spring Security usually prefixes roles with "ROLE_", e.g., "ROLE_ADMIN"
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ADMIN"));

            if (isAdmin) {
                return "redirect:/admin/products";
            }

            // 3. Check for COOKER role
            boolean isCooker = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("COOKER"));

            if (isCooker) {
                return "redirect:/kitchen";
            }
        }

        // 4. Default fallback for other users or anonymous
        return "redirect:/menu";
    }

    @GetMapping("/menu")
    public String menu(Model model) {
        List<Product> menu = productService.getMenuForOrder();
        model.addAttribute("activePage", "menu");
        model.addAttribute("menu", menu);
        model.addAttribute("categories", MenuCategory.values());
        return "menu";
    }
}
