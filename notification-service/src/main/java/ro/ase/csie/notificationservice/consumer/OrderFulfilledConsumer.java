package ro.ase.csie.notificationservice.consumer;

import ro.ase.csie.notificationservice.service.NotificationDispatchService;
import ro.ase.csie.notificationservice.service.XsltTransformerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderFulfilledConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderFulfilledConsumer.class);
    private static final String NOTIFICATION_XSL = "xslt/fulfillment-to-notification.xsl";

    private final XsltTransformerService xsltTransformerService;
    private final NotificationDispatchService dispatchService;
    private final String trackingUrlBase;

    public OrderFulfilledConsumer(
            XsltTransformerService xsltTransformerService,
            NotificationDispatchService dispatchService,
            @Value("${notification.tracking-url-base}") String trackingUrlBase) {
        this.xsltTransformerService = xsltTransformerService;
        this.dispatchService = dispatchService;
        this.trackingUrlBase = trackingUrlBase;
    }

    @KafkaListener(topics = "order.fulfilled")
    public void onOrderFulfilled(String fulfillmentEventXml) {
        try {
            log.debug("Received FulfillmentEvent XML:\n{}", fulfillmentEventXml);

            // Extract orderId for correlated logging before full XSLT processing
            var matcher = java.util.regex.Pattern.compile("<orderId>([^<]+)</orderId>")
                    .matcher(fulfillmentEventXml);
            if (matcher.find()) {
                MDC.put("orderId", matcher.group(1));
            }

            String notificationXml = xsltTransformerService.transform(
                    fulfillmentEventXml, NOTIFICATION_XSL,
                    Map.of("trackingUrlBase", trackingUrlBase));
            dispatchService.dispatch(notificationXml);

        } catch (RuntimeException e) {
            log.error("Failed to process FulfillmentEvent and dispatch notification", e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
