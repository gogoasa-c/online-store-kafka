package ro.ase.csie.orderservice.controller;

import io.vavr.control.Try;
import ro.ase.csie.orderservice.dto.CreateOrderDto;
import ro.ase.csie.orderservice.dto.OrderAcceptedResponse;
import ro.ase.csie.orderservice.service.OrderPublisherService;
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
        return Try.of(() -> publisherService.publishOrder(dto))
                .<ResponseEntity<?>>mapTry(orderId -> ResponseEntity
                        .status(HttpStatus.ACCEPTED)
                        .body(new OrderAcceptedResponse(orderId, "ACCEPTED")))
                .getOrElseGet(e -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to serialize order: " + e.getMessage()));

    }
}
