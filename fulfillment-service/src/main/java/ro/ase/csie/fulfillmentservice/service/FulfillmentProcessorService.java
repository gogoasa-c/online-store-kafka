package ro.ase.csie.fulfillmentservice.service;

import io.vavr.control.Try;
import ro.ase.csie.shared.models.FulfillmentEvent;
import ro.ase.csie.shared.models.OrderRequest;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDate;

@Service
public class FulfillmentProcessorService {

    private static final Logger log = LoggerFactory.getLogger(FulfillmentProcessorService.class);

    private final String warehouseId;
    private final int estimatedDeliveryDays;
    private final JAXBContext jaxbContext;

    public FulfillmentProcessorService(
            @Value("${fulfillment.warehouse-id}") String warehouseId,
            @Value("${fulfillment.estimated-delivery-days}") int estimatedDeliveryDays)
            throws JAXBException {
        this.warehouseId = warehouseId;
        this.estimatedDeliveryDays = estimatedDeliveryDays;
        this.jaxbContext = JAXBContext.newInstance(FulfillmentEvent.class);
    }

    /**
     * Simulates warehouse packing, builds an immutable FulfillmentEvent,
     * and marshals it to XML. Pure construction + marshalling — no mutable state.
     *
     * @param order the incoming order to fulfill
     * @return serialized FulfillmentEvent XML
     * @throws JAXBException        if JAXB marshalling fails
     * @throws InterruptedException if the warehouse simulation sleep is interrupted
     */
    public String process(final OrderRequest order) throws JAXBException, InterruptedException {
        log.info("Processing order {}... simulating warehouse packing", order.getOrderId());
        Thread.sleep(1000);

        final FulfillmentEvent event = FulfillmentEvent.of(order, warehouseId, estimatedDeliveryDays);

        // Try.of wraps the checked JAXBException in a composable value (like Scala's Try[String])
        final String xml = Try.of(() -> marshal(event))
                .getOrElseThrow(e -> e instanceof JAXBException je
                        ? je : new JAXBException(e.getMessage()));

        log.info("Dispatching FulfillmentEvent for order {}", order.getOrderId());

        return xml;
    }

    private String marshal(final FulfillmentEvent event) throws JAXBException {
        final Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        final StringWriter writer = new StringWriter();
        marshaller.marshal(event, writer);
        return writer.toString();
    }
}
