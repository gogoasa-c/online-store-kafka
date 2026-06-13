package com.example.orderservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ShippingAddressDto(
        @NotBlank String street,
        @NotBlank String city,
        @NotBlank String zip
) {}
