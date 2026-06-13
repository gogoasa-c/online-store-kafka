package com.example.notificationservice.consumer;

import com.example.notificationservice.service.NotificationDispatchService;
import com.example.notificationservice.service.XsltTransformerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderFulfilledConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderFulfilledConsumer.class);
    private static final String NOTIFICATION_XSL = "xslt/fulfillment-to-notification.xsl";

    private final XsltTransformerService xsltTransformerService;
    private final NotificationDispatchService dispatchService;

    public OrderFulfilledConsumer(
            XsltTransformerService xsltTransformerService,
            NotificationDispatchService dispatchService) {
        this.xsltTransformerService = xsltTransformerService;
        this.dispatchService = dispatchService;
    }

    @KafkaListener(topics = "order.fulfilled", groupId = "notification-group")
    public void onOrderFulfilled(String fulfillmentEventXml) {
        try {
            log.debug("Received FulfillmentEvent XML:\n{}", fulfillmentEventXml);

            String notificationXml = xsltTransformerService.transform(fulfillmentEventXml, NOTIFICATION_XSL);
            dispatchService.dispatch(notificationXml);

        } catch (Exception e) {
            log.error("Failed to process FulfillmentEvent and dispatch notification", e);
        }
    }
}
