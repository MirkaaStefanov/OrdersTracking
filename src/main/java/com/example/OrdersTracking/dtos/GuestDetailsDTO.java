package com.example.OrdersTracking.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GuestDetailsDTO {
    private String email;
    private String phone;
    private String location;

}
