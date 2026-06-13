package com.example.orderservice.controller;

import com.example.orderservice.dto.CreateOrderDto;
import com.example.orderservice.service.OrderPublisherService;
import jakarta.xml.bind.JAXBException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderPublisherService publisherService;

    public OrderController(OrderPublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> placeOrder(@RequestBody CreateOrderDto dto) {
        try {
            String orderId = publisherService.publishOrder(dto);
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(Map.of("orderId", orderId, "status", "ACCEPTED"));
        } catch (JAXBException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to serialize order: " + e.getMessage()));
        }
    }
}
