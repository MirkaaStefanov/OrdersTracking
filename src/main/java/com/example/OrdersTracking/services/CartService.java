package com.example.OrdersTracking.services;

import com.example.OrdersTracking.models.Cart;
import com.example.OrdersTracking.models.CartItem;
import com.example.OrdersTracking.models.Product;
import com.example.OrdersTracking.repositories.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private ProductRepository productRepository; // Единственото репо, от което се нуждаем

    public static final String CART_SESSION_KEY = "shoppingCart";

    /**
     * Взема количката от сесията. Ако не съществува, създава нова.
     */
    public Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute(CART_SESSION_KEY);

        if (cart == null) {
            cart = new Cart();
            cart.setCartItems(new ArrayList<>()); // Инициализираме листа
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public void addProductToCart(HttpSession session, Long productId, int quantity) {
        // 1. Вземи продукта от базата данни
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Продукт с ID " + productId + " не е намерен"));

        // 2. Вземи количката от сесията
        Cart cart = getCart(session);
        List<CartItem> cartItems = cart.getCartItems();

        // 3. Провери дали продуктът вече е в количката
        Optional<CartItem> existingItem = cartItems.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Ако да, просто увеличи количеството
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            // Ако не, създай нов CartItem и го добави
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            // Забележка: newItem.setCart(cart) не е нужно тук,
            // защото обектът cart не се пази в DB и двупосочната връзка не е критична
            cartItems.add(newItem);
        }

        // 4. Запази променената количка обратно в сесията
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    /**
     * Премахва продукт изцяло от количката в сесията.
     */
    public void removeProductFromCart(HttpSession session, Long productId) {
        Cart cart = getCart(session);
        cart.getCartItems().removeIf(item -> item.getProduct().getId().equals(productId));
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    /**
     * Обновява количеството на даден продукт в количката.
     */
    public void updateProductQuantity(HttpSession session, Long productId, int quantity) {
        if (quantity <= 0) {
            // Ако количеството е 0 или по-малко, премахни продукта
            removeProductFromCart(session, productId);
            return;
        }

        Cart cart = getCart(session);
        Optional<CartItem> itemOpt = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (itemOpt.isPresent()) {
            itemOpt.get().setQuantity(quantity);
            session.setAttribute(CART_SESSION_KEY, cart);
        }
    }

    /**
     * Изчиства количката изцяло.
     */
    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    public BigDecimal calculateTotalPrice(HttpSession session) {
        Cart cart = getCart(session);

        // Use Java Streams to sum up (Price * Quantity) for every item
        return cart.getCartItems().stream()
                .map(item -> {
                    BigDecimal price = item.getProduct().getPrice();
                    BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());

                    return price.multiply(quantity);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
