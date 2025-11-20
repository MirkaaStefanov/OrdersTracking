package com.example.OrdersTracking.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- ВРЪЗКА КЪМ ГЛАВНАТА ПОРЪЧКА ---
    // Много OrderItems принадлежат на един Order
    // nullable = false: Артикулът НЕ МОЖЕ да съществува без поръчка
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude // Изключваме от ToString
    private Order order;

    // --- ВРЪЗКА КЪМ ПРОДУКТА ---
    // Много OrderItems могат да сочат към един Product
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product; // Връзка към вашия Product @Entity

    // --- ДАННИ ЗА АРТИКУЛА ---
    @Column(nullable = false)
    private int quantity;

    // --- "СНИМКА" НА ДАННИТЕ В МОМЕНТА НА ПОКУПКА ---
    // ТОВА Е КРИТИЧНО! Не вземайте цената от Product,
    // защото тя може да се промени. Запазете я тук.

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase; // Цената в лв. в момента на поръчката

    @Column(precision = 10, scale = 2)
    private BigDecimal euroPriceAtPurchase; // Цената в евро (от вашия Product)

    private int grAtPurchase; // Грамажът (от вашия Product)
}
