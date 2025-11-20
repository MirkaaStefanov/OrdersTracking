package com.example.OrdersTracking.enums;

public enum OrderStatus {
    PAYMENT,
    PROCESSING,     // Поръчката е приета, чака потвърждение
    IN_DELIVERY,
    READY,// Изпратена за доставка
    COMPLETED,      // Доставена и завършена
    CANCELLED       // Отказана
}
