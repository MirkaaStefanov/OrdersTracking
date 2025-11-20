package com.example.OrdersTracking.services;

import com.example.OrdersTracking.dtos.GuestDetailsDTO;
import com.example.OrdersTracking.enums.OrderStatus;
import com.example.OrdersTracking.models.Cart;
import com.example.OrdersTracking.models.CartItem;
import com.example.OrdersTracking.models.Order;
import com.example.OrdersTracking.models.OrderItem;
import com.example.OrdersTracking.repositories.OrderItemRepository;
import com.example.OrdersTracking.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

// Трябва да създадете @Entity "Order" и "OrderItem"
// Този код е само ПРИМЕРЕН и зависи от вашите Entity-та

@Service
public class OrderService {

    // Трябва да създадете OrderRepository и OrderItemRepository
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private SseService sseService;

    // @Autowired private EmailService emailService; // За изпращане на имейли

    /**
     * Това е основният метод!
     * 1. Взема Cart (от сесия) и GuestDetails (от форма)
     * 2. Конвертира ги в Order и OrderItem (за DB)
     * 3. Записва ги в DB
     * 4. Генерира токен за достъп
     * 5. Изпраща имейл
     */
    @Transactional // Много важно!
    public Order createGuestOrder(Cart cart, GuestDetailsDTO guestDetails) {

        // 1. Създаваме новата поръчка (Order)
        Order order = new Order();
        order.setCustomerEmail(guestDetails.getEmail());
        order.setCustomerPhone(guestDetails.getPhone());
        order.setLocation(guestDetails.getLocation());

        if (guestDetails.isCash()) {
            order.setStatus(OrderStatus.PROCESSING); // Cash payment -> start processing immediately
        } else {
            order.setStatus(OrderStatus.PAYMENT);    // Card payment -> wait for Stripe confirmation
        }

        // Генерираме "вълшебния линк" токен (от първия ни разговор)
        order.setAccessToken(UUID.randomUUID().toString());

        BigDecimal total = cart.getCartItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        order.setTotalPrice(total);

        // 2. Запазваме поръчката, за да получи ID
        Order savedOrder = orderRepository.save(order);

        // 3. Конвертираме CartItems в OrderItems
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder); // Свързваме с току-що запазената поръчка
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(cartItem.getProduct().getPrice()); // Добра практика

            orderItems.add(orderItem);
        }

        // 4. Запазваме всички артикули от поръчката
        List<OrderItem> savedItems = orderItemRepository.saveAll(orderItems);
        savedOrder.setItems(savedItems);
        Order sendOrder = orderRepository.save(savedOrder);

        if(guestDetails.isCash()){
            sseService.sendNewOrderEvent(sendOrder);
        }

        return savedOrder;
    }

    // Трябва да имплементирате и методите от първия ни разговор
    public Optional<Order> findOrderByAccessToken(String token) {
        // return orderRepository.findByAccessToken(token);
        return Optional.empty(); // Засега
    }

    @Transactional
    public void successPayment(Long orderId) throws ChangeSetPersister.NotFoundException {
        Order order = orderRepository.findById(orderId).orElseThrow(ChangeSetPersister.NotFoundException::new);
        order.setStatus(OrderStatus.PROCESSING);
        Order savedOrder = orderRepository.save(order);
        sseService.sendNewOrderEvent(savedOrder);
    }
    //TODO send email to track order

    public List<Order> ordersForKitchen(){
        List<Order> allOrders = orderRepository.findAll();

        // 2. Filter by Status and Map to DTO
        return allOrders.stream()
                // 1. Filter: Keep only orders with status PROCESSING
                .filter(order -> order.getStatus() == OrderStatus.PROCESSING)

                // 2. Sort: Use Comparator to sort by orderDate (oldest first)
                .sorted(Comparator.comparing(Order::getOrderDate))

                // 3. Collect: Gather the sorted list
                .collect(Collectors.toList());

    }

}