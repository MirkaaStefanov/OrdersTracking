package com.example.OrdersTracking.enums;

public enum OrderStatus {
    PROCESSING,     // Поръчката е приета, чака потвърждение
    CONFIRMED,      // Ресторантът е потвърдил поръчката
    IN_DELIVERY,    // Изпратена за доставка
    COMPLETED,      // Доставена и завършена
    CANCELLED       // Отказана
}
