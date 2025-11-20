package com.example.OrdersTracking.controllers;

import com.example.OrdersTracking.dtos.GuestDetailsDTO;
import com.example.OrdersTracking.models.Cart;
import com.example.OrdersTracking.models.Order;
import com.example.OrdersTracking.services.CartService;
import com.example.OrdersTracking.services.OrderService;
import com.example.OrdersTracking.services.StripeService;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;// Трябва да създадете този Service!

    @Autowired
    private StripeService stripeService;
    /**
     * GET /checkout
     * Показва страницата за въвеждане на данни (имейл, телефон, адрес)
     */
    @GetMapping
    public String showCheckoutPage(HttpSession session, Model model) {
        Cart cart = cartService.getCart(session);

        // If cart is empty, redirect back
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return "redirect:/cart";
        }

        // 1. Calculate Total in Base Currency (BGN)
        BigDecimal totalPrice = cartService.calculateTotalPrice(session);

        // 2. Calculate Total in Euro
        // Formula: Price / 1.95583
        BigDecimal exchangeRate = new BigDecimal("1.95583");
        BigDecimal totalEuro = totalPrice.divide(exchangeRate, 2, RoundingMode.HALF_UP);

        // 3. Add attributes to the model
        model.addAttribute("cart", cart);
        model.addAttribute("guestDetails", new GuestDetailsDTO());

        // Add the calculated prices
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("totalEuro", totalEuro);

        return "checkout";
    }


    @PostMapping
    public String placeOrder(HttpSession session,
                             @ModelAttribute("guestDetails") GuestDetailsDTO guestDetails,
                             RedirectAttributes redirectAttributes) {

        Cart cart = cartService.getCart(session);

        if (cart.getCartItems().isEmpty()) {
            return "redirect:/cart";
        }

        try {
            // 1. Прехвърляме количката и данните на OrderService.
            // OrderService ще създаде поръчката и ще зададе началния статус.
            Order newOrder = orderService.createGuestOrder(cart, guestDetails);

            if (guestDetails.isCash()) {
                // Cash payment (Pay on delivery)
                // 1. OrderService is updated to set the initial status to PROCESSING when isCash is false.
                // 2. Clear the cart immediately.
                cartService.clearCart(session);

                // 3. Redirect to success page.
                redirectAttributes.addFlashAttribute("message", "Вашата поръчка е приета!");
                return "redirect:/checkout/success";

            } else {
                // Card payment (Stripe)
                // 1. OrderService is updated to set the initial status to PAYMENT when isCash is true.

                // 2. Create the Stripe Session.
                Session stripeSession = stripeService.createCheckoutSession(newOrder);

                // 3. Redirect to Stripe's payment page. Cart will be cleared on Stripe success webhook.
                return "redirect:" + stripeSession.getUrl();
            }

        } catch (Exception e) {
            // При грешка (напр. продуктът вече не е наличен ИЛИ Stripe грешка)
            redirectAttributes.addFlashAttribute("error", "Грешка при създаване на поръчката: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/success")
    public String successPage(){
        return "order-success";
    }

    @GetMapping("/stripe-success")
    public String handleStripeSuccess(@RequestParam("session_id") String sessionId,
                                      @RequestParam("order_id") Long orderId,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        try {
            // 1. Retrieve the session from Stripe to verify payment status
            Session stripeSession = stripeService.retrieveSession(sessionId);

            // 2. Check if the payment status is definitively 'paid'
            if ("paid".equalsIgnoreCase(stripeSession.getPaymentStatus())) {

                orderService.successPayment(orderId);

                // 4. Clear the user's cart
                cartService.clearCart(session);

                redirectAttributes.addFlashAttribute("message", "Плащането е успешно! Вашата поръчка е приета и се обработва.");
                return "redirect:/checkout/success";
            } else {
                // Payment was not paid (e.g., pending, failed)
                redirectAttributes.addFlashAttribute("error", "Плащането не е завършено (Статус: " + stripeSession.getPaymentStatus() + "). Моля опитайте отново.");
                // Optionally update order to CANCELLED here
                return "redirect:/checkout";
            }

        } catch (Exception e) {
            // Handle Stripe errors, ID not found errors, etc.
            redirectAttributes.addFlashAttribute("error", "Грешка при финализиране на поръчката: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    /**
     * GET /checkout/stripe-cancel
     * Called by Stripe when the user cancels the payment or an error occurs on Stripe's side.
     */
    @GetMapping("/stripe-cancel")
    public String handleStripeCancel(@RequestParam("order_id") Long orderId,
                                     RedirectAttributes redirectAttributes) {

        // 1. Optionally update the order to CANCELLED in the database
        // orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED);

        // 2. Add an error message for the user
        redirectAttributes.addFlashAttribute("error", "Плащането беше отказано. Моля опитайте отново или изберете плащане в брой.");

        // 3. Redirect back to the checkout form
        return "redirect:/checkout";
    }

}
