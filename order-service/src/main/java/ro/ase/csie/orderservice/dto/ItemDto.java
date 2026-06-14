package ro.ase.csie.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ItemDto(
        @NotBlank String sku,
        @Positive int qty,
        @PositiveOrZero double price
) {}
