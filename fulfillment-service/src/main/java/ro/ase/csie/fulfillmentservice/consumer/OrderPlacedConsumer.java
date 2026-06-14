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
import java.util.Optional;

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
        Try.of(orderRequestContext::createUnmarshaller)
                .mapTry(unmarshaller -> (OrderRequest) unmarshaller.unmarshal(new StringReader(orderXml)))
                .peek(order -> MDC.put("orderId", order.getOrderId()))
                .peek(order -> log.info("Fulfillment Work Item XML:\n{}", xsltTransformerService.transform(orderXml, WORK_ITEM_XSL)))
                .mapTry(order -> Tuple.of(order.getOrderId(), processorService.process(order)))
                .peek(tuple ->
                        kafkaTemplate.send(OUTPUT_TOPIC, tuple._1, tuple._2)
                                .whenComplete((result, exception) -> Optional.ofNullable(exception)
                                        .ifPresentOrElse(e -> log.error("Failed to publish FulfillmentEvent for order {}: {}", tuple._1, e.getMessage()),
                                                () -> log.info("Published FulfillmentEvent for order {} to topic {}", tuple._1, OUTPUT_TOPIC))))
                .onFailure(e -> {
                    if (e instanceof InterruptedException) {
                       Thread.currentThread().interrupt();
                    }

                    log.error("Fulfillment processing failed for incoming message", e);
                })
                .andFinally(MDC::clear);
    }
}
