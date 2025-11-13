package com.example.OrdersTracking.controllers;

import com.example.OrdersTracking.models.Cart;
import com.example.OrdersTracking.services.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/cart") // Всички URL-и ще започват с /cart
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Cart cart = cartService.getCart(session);
        BigDecimal totalPriceBigDecimal = cart.getCartItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("activePage", "cart");
        model.addAttribute("totalPrice", totalPriceBigDecimal);
        model.addAttribute("cart", cart);
        return "cart"; // Трябва да имате 'cart.html' в templates
    }

    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable("productId") Long productId,
                            @RequestParam("quantity") int quantity,
                            HttpSession session) {

        cartService.addProductToCart(session, productId, quantity);

        return "redirect:/"; // или "redirect:/menu"
    }


    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable("productId") Long productId,
                                 HttpSession session) {

        cartService.removeProductFromCart(session, productId);


        return "redirect:/cart";
    }


    @PostMapping("/update/{productId}")
    public String updateQuantity(@PathVariable("productId") Long productId,
                                 @RequestParam("quantity") int quantity,
                                 HttpSession session) {

        cartService.updateProductQuantity(session, productId, quantity);

        return "redirect:/cart";
    }
}
