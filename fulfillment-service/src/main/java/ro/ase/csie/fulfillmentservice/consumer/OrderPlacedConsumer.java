package ro.ase.csie.fulfillmentservice.consumer;

import ro.ase.csie.fulfillmentservice.model.OrderRequest;
import ro.ase.csie.fulfillmentservice.service.FulfillmentProcessorService;
import ro.ase.csie.fulfillmentservice.service.XsltTransformerService;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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

    @KafkaListener(topics = "order.placed")
    public void onOrderPlaced(String orderXml) {
        try {
            Unmarshaller unmarshaller = orderRequestContext.createUnmarshaller();
            OrderRequest order = (OrderRequest) unmarshaller.unmarshal(new StringReader(orderXml));
            MDC.put("orderId", order.getOrderId());

            // XSLT produces a PII-stripped audit document for debug logging only; downstream
            // processing uses the full OrderRequest so the warehouse audit log stays separate.
            String workItemXml = xsltTransformerService.transform(orderXml, WORK_ITEM_XSL);
            log.debug("FulfillmentWorkItem XML:\n{}", workItemXml);

            String fulfillmentEventXml = processorService.process(order);

            String oId = order.getOrderId();
            kafkaTemplate.send(OUTPUT_TOPIC, oId, fulfillmentEventXml)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish FulfillmentEvent for order {} to '{}'", oId, OUTPUT_TOPIC, ex);
                        } else {
                            log.info("Published FulfillmentEvent to '{}' for order {}", OUTPUT_TOPIC, oId);
                        }
                    });

        } catch (JAXBException e) {
            log.error("Failed to unmarshal OrderRequest XML", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Fulfillment processing interrupted for message", e);
        } finally {
            MDC.clear();
        }
    }
}
