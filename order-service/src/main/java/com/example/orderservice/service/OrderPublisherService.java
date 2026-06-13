package com.example.orderservice.service;

import com.example.orderservice.dto.CreateOrderDto;
import com.example.orderservice.model.Item;
import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.model.ShippingAddress;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderPublisherService {

    private static final Logger log = LoggerFactory.getLogger(OrderPublisherService.class);
    private static final String TOPIC = "order.placed";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JAXBContext jaxbContext;

    public OrderPublisherService(KafkaTemplate<String, String> kafkaTemplate) throws JAXBException {
        this.kafkaTemplate = kafkaTemplate;
        this.jaxbContext = JAXBContext.newInstance(OrderRequest.class);
    }

    public String publishOrder(CreateOrderDto dto) throws JAXBException {
        String orderId = UUID.randomUUID().toString();

        OrderRequest order = new OrderRequest();
        order.setOrderId(orderId);
        order.setCustomerId(dto.customerId());
        order.setCustomerEmail(dto.customerEmail());
        order.setPaymentStatus(dto.paymentStatus());
        order.setTimestamp(Instant.now().toString());

        ShippingAddress address = new ShippingAddress(
                dto.shippingAddress().street(),
                dto.shippingAddress().city(),
                dto.shippingAddress().zip()
        );
        order.setShippingAddress(address);

        List<Item> items = dto.items().stream()
                .map(i -> new Item(i.sku(), i.qty(), i.price()))
                .toList();
        order.setItems(items);

        String xml = marshal(order);
        log.info("Publishing OrderRequest to topic '{}': orderId={}", TOPIC, orderId);
        kafkaTemplate.send(TOPIC, orderId, xml);

        return orderId;
    }

    private String marshal(OrderRequest order) throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter writer = new StringWriter();
        marshaller.marshal(order, writer);
        return writer.toString();
    }
}
