package com.example.OrdersTracking.services;

import com.example.OrdersTracking.models.Order;
import com.example.OrdersTracking.models.OrderItem;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class StripeService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${app.base.url}")
    private String baseURL;

    private static final BigDecimal EURO_EXCHANGE_RATE = new BigDecimal("1.95583");

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public Session retrieveSession(String sessionId) throws StripeException {
        return Session.retrieve(sessionId);
    }

    public Session createCheckoutSession(Order order) throws StripeException {

        // 1. Start building the session parameters
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)

                // VVVV --- MODIFIED URLs --- VVVV
                // Success URL points to a new controller method
                .setSuccessUrl(baseURL + "/checkout/stripe-success?session_id={CHECKOUT_SESSION_ID}&order_id=" + order.getId())
                // Cancel URL points to a new controller method
                .setCancelUrl(baseURL + "/checkout/stripe-cancel?order_id=" + order.getId())
                // ^^^^ --- MODIFIED URLs --- ^^^^

                .setCustomerEmail(order.getCustomerEmail());

        // 2. Add metadata (helps match payment to order later)
        paramsBuilder.putMetadata("order_id", order.getId().toString());

        // 3. Loop through Order Items and add them to Stripe Session
        for (OrderItem item : order.getItems()) {
            paramsBuilder.addLineItem(createLineItem(item));
        }

        // 4. Create and return the session
        return Session.create(paramsBuilder.build());
    }

    private SessionCreateParams.LineItem createLineItem(OrderItem item) {
        // Calculate price in Euro
        BigDecimal priceInLev = item.getPriceAtPurchase(); // Ensure you use the price at moment of purchase
        BigDecimal priceInEuro = priceInLev.divide(EURO_EXCHANGE_RATE, 2, RoundingMode.HALF_UP);

        // Stripe expects the amount in Cents (Long) -> 10.50 EUR = 1050 cents
        long priceInCents = priceInEuro.multiply(new BigDecimal("100")).longValue();

        return SessionCreateParams.LineItem.builder()
                .setQuantity((long) item.getQuantity())
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount(priceInCents)
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(item.getProduct().getName())
                                                .build())
                                .build())
                .build();
    }
}
