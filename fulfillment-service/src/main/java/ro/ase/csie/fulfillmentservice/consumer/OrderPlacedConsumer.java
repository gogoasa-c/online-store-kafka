package ro.ase.csie.fulfillmentservice.consumer;

import io.vavr.Tuple;
import io.vavr.control.Try;
import ro.ase.csie.shared.models.OrderRequest;
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
    public void onOrderPlaced(final String orderXml) {
        // Pipeline: unmarshal → log XSLT debug output → process → publish
        // Tuple.of carries (orderId, fulfillmentXml) across the pipeline boundary (Scala: (String, String))
        Try.of(() -> {
                    final Unmarshaller unmarshaller = orderRequestContext.createUnmarshaller();
                    return (OrderRequest) unmarshaller.unmarshal(new StringReader(orderXml));
                })
                .peek(order -> MDC.put("orderId", order.getOrderId()))
                .mapTry(order -> {
                    final String workItemXml = xsltTransformerService.transform(orderXml, WORK_ITEM_XSL);
                    log.debug("FulfillmentWorkItem XML:\n{}", workItemXml);
                    // Tuple pairs orderId + fulfillmentXml so both are available for the Kafka send
                    return Tuple.of(order.getOrderId(), processorService.process(order));
                })
                .peek(t -> kafkaTemplate.send(OUTPUT_TOPIC, t._1(), t._2())
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to publish FulfillmentEvent for order {} to '{}'",
                                        t._1(), OUTPUT_TOPIC, ex);
                            } else {
                                log.info("Published FulfillmentEvent to '{}' for order {}",
                                        OUTPUT_TOPIC, t._1());
                            }
                        }))
                .onFailure(e -> {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    log.error("Fulfillment processing failed for incoming message", e);
                })
                .andFinally(MDC::clear);
    }
}
