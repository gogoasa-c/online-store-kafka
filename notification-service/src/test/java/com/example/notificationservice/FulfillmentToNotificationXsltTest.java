package com.example.notificationservice;

import com.example.notificationservice.service.XsltTransformerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FulfillmentToNotificationXsltTest {

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

    @Test
    void transform_producesWellFormedNotificationPayload() {
        String result = xsltTransformerService.transform(SAMPLE_FULFILLMENT_XML, "xslt/fulfillment-to-notification.xsl");

        assertThat(result).contains("<notificationPayload>");
    }

    @Test
    void transform_mapsCustomerEmailToRecipientEmail() {
        String result = xsltTransformerService.transform(SAMPLE_FULFILLMENT_XML, "xslt/fulfillment-to-notification.xsl");

        assertThat(result).contains("<recipientEmail>diana@example.com</recipientEmail>");
    }

    @Test
    void transform_constructsSubjectWithOrderId() {
        String result = xsltTransformerService.transform(SAMPLE_FULFILLMENT_XML, "xslt/fulfillment-to-notification.xsl");

        assertThat(result).contains("TEST-ORDER-001");
        assertThat(result).contains("has shipped!");
    }

    @Test
    void transform_constructsTrackingUrlFromTrackingCode() {
        String result = xsltTransformerService.transform(SAMPLE_FULFILLMENT_XML, "xslt/fulfillment-to-notification.xsl");

        assertThat(result).contains("<trackingUrl>https://track.example.com/TRK-TEST-ORD</trackingUrl>");
    }

    @Test
    void transform_orderSummaryContainsWarehouseAndDispatchInfo() {
        String result = xsltTransformerService.transform(SAMPLE_FULFILLMENT_XML, "xslt/fulfillment-to-notification.xsl");

        assertThat(result).contains("WH-BUC-01");
        assertThat(result).contains("2026-06-13T10:01:30Z");
        assertThat(result).contains("2026-06-16");
    }
}
