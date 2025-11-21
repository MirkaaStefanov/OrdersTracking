package com.example.OrdersTracking.models;

import com.example.OrdersTracking.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders") // "order" често е запазена дума в SQL, затова "orders" е по-безопасно
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    @ToString.Exclude // Изключваме от ToString, за да избегнем рекурсия
    private User user; // Трябва да имате User @Entity клас

    // --- ДАННИ ЗА ГОСТ (Винаги се попълват) ---
    // Дори регистриран потребител да направи поръчка,
    // е добре да запазим имейла, с който е направена
    @Column(nullable = false)
    private String customerEmail;

    private String customerPhone;
    private boolean cash;
    private String location; // Местоположение/адрес

    // --- ДАННИ ЗА ПОРЪЧКАТА ---
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // Използвайте BigDecimal за пари!
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // --- "ВЪЛШЕБЕН ЛИНК" ЗА ГОСТИ ---
    // Уникален, труден за отгатване токен за достъп
    @Column(nullable = false, unique = true)
    private String accessToken;

    // --- АРТИКУЛИ В ПОРЪЧКАТА ---
    // Една поръчка има много OrderItems
    // cascade = CascadeType.ALL: Ако изтрием Order, изтрий и OrderItems
    // orphanRemoval = true: Ако премахнем OrderItem от този списък, изтрий го от DB
    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items;

    // Метод за удобство при създаване на поръчката
    @PrePersist
    public void prePersist() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
    }
}
