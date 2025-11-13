package com.example.OrdersTracking.controllers;

import com.example.OrdersTracking.models.Cart;
import com.example.OrdersTracking.services.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.example.OrdersTracking.controllers") // **ВАЖНО: Посочете пакета с вашите *публични* контролери**
public class GlobalControllerAdvice {

    @Autowired
    private CartService cartService;

    /**
     * Този метод се изпълнява преди всеки контролер в посочения пакет.
     * Той взема количката от сесията и добавя общия брой артикули към модела.
     */
    @ModelAttribute("cartItemCount")
    public Integer getCartItemCount(HttpSession session) {
        Cart cart = cartService.getCart(session);
        if (cart == null || cart.getCartItems() == null) {
            return 0;
        }

        // Това изчислява ОБЩИЯ брой продукти (напр. 2 супи + 1 салата = 3)
        return cart.getCartItems().stream()
                .mapToInt(item -> item.getQuantity())
                .sum();

        // Ако искате броя на УНИКАЛНИТЕ продукти (напр. 2 супи + 1 салата = 2),
        // използвайте това:
        // return cart.getCartItems().size();
    }
}
