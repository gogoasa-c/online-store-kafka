package com.example.orderservice.dto;

public record ShippingAddressDto(
        String street,
        String city,
        String zip
) {}
