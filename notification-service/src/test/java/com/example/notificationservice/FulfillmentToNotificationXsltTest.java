package com.example.notificationservice;

import com.example.notificationservice.service.XsltTransformerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FulfillmentToNotificationXsltTest {

    private static final String TRACKING_URL_BASE = "https://track.example.com/";
    private static final String XSL = "xslt/fulfillment-to-notification.xsl";

    private XsltTransformerService xsltTransformerService;

    private static final String SAMPLE_FULFILLMENT_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <fulfillmentEvent>
                <orderId>TEST-ORDER-001</orderId>
                <customerEmail>diana@example.com</customerEmail>
                <warehouseId>WH-BUC-01</warehouseId>
                <dispatchTimestamp>2026-06-13T10:01:30Z</dispatchTimestamp>
                <trackingCode>TRK-TEST-ORD</trackingCode>
                <estimatedDelivery>2026-06-16</estimatedDelivery>
            </fulfillmentEvent>
            """;

    @BeforeEach
    void setUp() {
        xsltTransformerService = new XsltTransformerService();
    }

    private String transform() {
        return xsltTransformerService.transform(SAMPLE_FULFILLMENT_XML, XSL,
                Map.of("trackingUrlBase", TRACKING_URL_BASE));
    }

    @Test
    void transform_producesWellFormedNotificationPayload() {
        assertThat(transform()).contains("<notificationPayload>");
    }

    @Test
    void transform_mapsCustomerEmailToRecipientEmail() {
        assertThat(transform()).contains("<recipientEmail>diana@example.com</recipientEmail>");
    }

    @Test
    void transform_constructsSubjectWithOrderId() {
        String result = transform();
        assertThat(result).contains("TEST-ORDER-001");
        assertThat(result).contains("has shipped!");
    }

    @Test
    void transform_constructsTrackingUrlFromTrackingCode() {
        assertThat(transform()).contains("<trackingUrl>https://track.example.com/TRK-TEST-ORD</trackingUrl>");
    }

    @Test
    void transform_orderSummaryContainsWarehouseAndDispatchInfo() {
        String result = transform();
        assertThat(result).contains("WH-BUC-01");
        assertThat(result).contains("2026-06-13T10:01:30Z");
        assertThat(result).contains("2026-06-16");
    }
}
