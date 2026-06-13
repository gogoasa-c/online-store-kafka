package com.example.orderservice.dto;

import java.util.List;

public record CreateOrderDto(
        String customerId,
        String customerEmail,
        List<ItemDto> items,
        ShippingAddressDto shippingAddress,
        String paymentStatus
) {}
