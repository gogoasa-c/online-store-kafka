package com.example.fulfillmentservice.consumer;

import com.example.fulfillmentservice.model.OrderRequest;
import com.example.fulfillmentservice.service.FulfillmentProcessorService;
import com.example.fulfillmentservice.service.XsltTransformerService;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.StringReader;

@Component
public class OrderPlacedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacedConsumer.class);
    private static final String OUTPUT_TOPIC = "order.fulfilled";
    private static final String WORK_ITEM_XSL = "xslt/order-to-fulfillment.xsl";

    private final XsltTransformerService xsltTransformerService;
    private final FulfillmentProcessorService processorService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JAXBContext orderRequestContext;

    public OrderPlacedConsumer(
            XsltTransformerService xsltTransformerService,
            FulfillmentProcessorService processorService,
            KafkaTemplate<String, String> kafkaTemplate) throws JAXBException {
        this.xsltTransformerService = xsltTransformerService;
        this.processorService = processorService;
        this.kafkaTemplate = kafkaTemplate;
        this.orderRequestContext = JAXBContext.newInstance(OrderRequest.class);
    }

    @KafkaListener(topics = "order.placed", groupId = "fulfillment-group")
    public void onOrderPlaced(String orderXml) {
        try {
            Unmarshaller unmarshaller = orderRequestContext.createUnmarshaller();
            OrderRequest order = (OrderRequest) unmarshaller.unmarshal(new StringReader(orderXml));

            String workItemXml = xsltTransformerService.transform(orderXml, WORK_ITEM_XSL);
            log.debug("FulfillmentWorkItem XML:\n{}", workItemXml);

            String fulfillmentEventXml = processorService.process(order);

            kafkaTemplate.send(OUTPUT_TOPIC, order.getOrderId(), fulfillmentEventXml);
            log.info("Published FulfillmentEvent to '{}' for order {}", OUTPUT_TOPIC, order.getOrderId());

        } catch (JAXBException e) {
            log.error("Failed to unmarshal OrderRequest XML", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Fulfillment processing interrupted for message", e);
        }
    }
}
