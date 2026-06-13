package com.example.fulfillmentservice.service;

import com.example.fulfillmentservice.model.FulfillmentEvent;
import com.example.fulfillmentservice.model.OrderRequest;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDate;

@Service
public class FulfillmentProcessorService {

    private static final Logger log = LoggerFactory.getLogger(FulfillmentProcessorService.class);
    private static final String WAREHOUSE_ID = "WH-BUC-01";

    private final JAXBContext jaxbContext;

    public FulfillmentProcessorService() throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(FulfillmentEvent.class);
    }

    public String process(OrderRequest order) throws JAXBException, InterruptedException {
        log.info("Processing order {}... simulating warehouse packing", order.getOrderId());
        Thread.sleep(1000);

        FulfillmentEvent event = new FulfillmentEvent();
        event.setOrderId(order.getOrderId());
        event.setCustomerEmail(order.getCustomerEmail());
        event.setWarehouseId(WAREHOUSE_ID);
        event.setDispatchTimestamp(Instant.now().toString());
        event.setTrackingCode("TRK-" + order.getOrderId().substring(0, 8).toUpperCase());
        event.setEstimatedDelivery(LocalDate.now().plusDays(3).toString());

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter writer = new StringWriter();
        marshaller.marshal(event, writer);

        log.info("Dispatching FulfillmentEvent for order {}", order.getOrderId());
        return writer.toString();
    }
}
