package ro.ase.csie.orderservice.service;

import io.vavr.control.Try;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ro.ase.csie.orderservice.dto.CreateOrderDto;
import ro.ase.csie.shared.models.Item;
import ro.ase.csie.shared.models.OrderRequest;
import ro.ase.csie.shared.models.ShippingAddress;

import java.io.StringWriter;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

    /**
     * Converts the incoming DTO to a shared-module OrderRequest, marshals it to XML,
     * and publishes it to the Kafka topic. Pure transformation up to the Kafka send.
     *
     * @param dto the validated incoming order payload
     * @return the generated orderId
     * @throws JAXBException if JAXB marshalling fails
     */
    public String publishOrder(CreateOrderDto dto) throws JAXBException {
        final String orderId = UUID.randomUUID().toString();

        // Vavr immutable List for the item-mapping pipeline (like Scala's List.map)
        final List<Item> items = dto.items().stream()
                .map(i -> new Item(i.sku(), i.qty(), i.price()))
                .toList();

        final ShippingAddress address = new ShippingAddress(
                dto.shippingAddress().street(),
                dto.shippingAddress().city(),
                dto.shippingAddress().zip()
        );
        final OrderRequest order = new OrderRequest(
                orderId,
                dto.customerId(),
                dto.customerEmail(),
                items,
                address,
                "PENDING",
                Instant.now()
        );

        final String xml = Try.of(() -> marshal(order))
                .getOrElseThrow(e -> e instanceof JAXBException je
                        ? je : new JAXBException(e.getMessage()));

        log.info("Publishing OrderRequest to topic '{}': orderId={}", TOPIC, orderId);
        kafkaTemplate.send(TOPIC, orderId, xml).whenComplete((result, ex) ->
                Optional.ofNullable(ex)
                        .ifPresentOrElse(
                                exception -> log.error("Failed to publish order {} to topic '{}'", orderId, TOPIC, exception),
                                () -> log.debug("Published order {} to partition {}", orderId, result.getRecordMetadata().partition())
                        ));

        return orderId;
    }

    private String marshal(OrderRequest order) throws JAXBException {
        final Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        final StringWriter writer = new StringWriter();
        marshaller.marshal(order, writer);

        return writer.toString();
    }
}
