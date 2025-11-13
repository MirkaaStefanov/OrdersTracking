package com.example.OrdersTracking.dtos;

import com.example.OrdersTracking.enums.MenuCategory;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    public Long id;

    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal euroPrice;
    private int gr;
    private MenuCategory category;
    private boolean available = true;

}
