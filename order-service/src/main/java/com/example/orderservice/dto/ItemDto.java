package com.example.orderservice.dto;

public record ItemDto(
        String sku,
        int qty,
        double price
) {}
