package com.example.orderservice.controller;

import com.example.orderservice.dto.CreateOrderDto;
import com.example.orderservice.dto.OrderAcceptedResponse;
import com.example.orderservice.service.OrderPublisherService;
import jakarta.validation.Valid;
import jakarta.xml.bind.JAXBException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderPublisherService publisherService;

    public OrderController(OrderPublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@Valid @RequestBody CreateOrderDto dto) {
        try {
            String orderId = publisherService.publishOrder(dto);
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(new OrderAcceptedResponse(orderId, "ACCEPTED"));
        } catch (JAXBException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to serialize order: " + e.getMessage());
        }
    }
}
