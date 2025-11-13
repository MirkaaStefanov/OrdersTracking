package com.example.OrdersTracking.controllers;

import com.example.OrdersTracking.dtos.GuestDetailsDTO;
import com.example.OrdersTracking.models.Cart;
import com.example.OrdersTracking.models.Order;
import com.example.OrdersTracking.services.CartService;
import com.example.OrdersTracking.services.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService; // Трябва да създадете този Service!

    /**
     * GET /checkout
     * Показва страницата за въвеждане на данни (имейл, телефон, адрес)
     */
    @GetMapping
    public String showCheckoutPage(HttpSession session, Model model) {
        Cart cart = cartService.getCart(session);

        // Ако количката е празна, върни го обратно
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cart", cart);
        // Добавяме празен DTO обект, който Thymeleaf да попълни с формата
        model.addAttribute("guestDetails", new GuestDetailsDTO());

        return "checkout"; // Трябва да имате 'checkout.html'
    }

    @PostMapping
    public String placeOrder(HttpSession session,
                             @ModelAttribute("guestDetails") GuestDetailsDTO guestDetails,
                             RedirectAttributes redirectAttributes) {

        Cart cart = cartService.getCart(session);

        if (cart.getCartItems().isEmpty()) {
            // Още една проверка, ако се опита да изпрати празна количка
            return "redirect:/cart";
        }

        try {
            // 1. Прехвърляме количката и данните на OrderService
            // Този service ще я запише в DB и ще върне новата поръчка
            Order newOrder = orderService.createGuestOrder(cart, guestDetails);

            // 2. Изчистваме количката от сесията
            cartService.clearCart(session);

            // 3. Изпращаме "вълшебния линк" (това трябва да е вътре в orderService)
            // orderService.sendConfirmationEmail(newOrder);

            // 4. Пренасочваме към страница "Благодарим"
            redirectAttributes.addFlashAttribute("message", "Вашата поръчка е приета!");
            return "redirect:/order/success"; // Специална "thank you" страница

        } catch (Exception e) {
            // При грешка (напр. продуктът вече не е наличен)
            redirectAttributes.addFlashAttribute("error", "Грешка при създаване на поръчката: " + e.getMessage());
            return "redirect:/checkout";
        }
    }
}
