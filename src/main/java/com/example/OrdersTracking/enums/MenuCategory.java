package com.example.OrdersTracking.enums;

import lombok.Data;


public enum MenuCategory {
    SALADS("Салати"),
    SOUPS("Супи"),
    MAIN_DISHES("Основни Ястия"),
    PURLENKAS("Пърленки"),
    DESSERTS("Десерти");

    private final String displayName;

    MenuCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
