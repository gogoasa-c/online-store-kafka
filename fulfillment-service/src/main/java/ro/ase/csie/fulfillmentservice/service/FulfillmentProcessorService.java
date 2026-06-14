package ro.ase.csie.fulfillmentservice.service;

import ro.ase.csie.fulfillmentservice.model.FulfillmentEvent;
import ro.ase.csie.fulfillmentservice.model.OrderRequest;
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

    public String process(OrderRequest order) throws JAXBException, InterruptedException {
        log.info("Processing order {}... simulating warehouse packing", order.getOrderId());
        Thread.sleep(1000);

        FulfillmentEvent event = new FulfillmentEvent(
                order.getOrderId(),
                order.getCustomerEmail(),
                warehouseId,
                Instant.now(),
                "TRK-" + order.getOrderId().substring(0, 8).toUpperCase(),
                LocalDate.now().plusDays(estimatedDeliveryDays)
        );

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter writer = new StringWriter();
        marshaller.marshal(event, writer);

        log.info("Dispatching FulfillmentEvent for order {}", order.getOrderId());
        return writer.toString();
    }
}
