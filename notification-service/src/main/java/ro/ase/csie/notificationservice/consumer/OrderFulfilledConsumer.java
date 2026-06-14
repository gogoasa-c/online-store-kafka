package ro.ase.csie.notificationservice.consumer;

import io.vavr.control.Option;
import io.vavr.control.Try;
import ro.ase.csie.notificationservice.service.NotificationDispatchService;
import ro.ase.csie.notificationservice.service.XsltTransformerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OrderFulfilledConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderFulfilledConsumer.class);
    private static final String NOTIFICATION_XSL = "xslt/fulfillment-to-notification.xsl";
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("<orderId>([^<]+)</orderId>");

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
    public void onOrderFulfilled(final String fulfillmentEventXml) {
        log.debug("Received FulfillmentEvent XML:\n{}", fulfillmentEventXml);

        // Option.of extracts orderId for correlated logging before full XSLT processing.
        // Equivalent to Scala's Option[String] — None when the pattern is absent, Some when matched.
        final Matcher matcher = ORDER_ID_PATTERN.matcher(fulfillmentEventXml);
        Option.of(matcher.find() ? matcher.group(1) : null)
                .peek(orderId -> MDC.put("orderId", orderId));

        // Try pipeline: transform → dispatch → log failure → clear MDC → re-throw on failure.
        // andThen carries the notification XML as a side-effectful dispatch step.
        Try.of(() -> xsltTransformerService.transform(
                        fulfillmentEventXml, NOTIFICATION_XSL,
                        Map.of("trackingUrlBase", trackingUrlBase)))
                .andThen(dispatchService::dispatch)
                .onFailure(e -> log.error(
                        "Failed to process FulfillmentEvent and dispatch notification", e))
                .andFinally(MDC::clear)
                .getOrElseThrow(e -> e instanceof RuntimeException re
                        ? re : new RuntimeException(e));
    }
}
