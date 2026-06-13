package com.example.fulfillmentservice;

import com.example.fulfillmentservice.service.XsltTransformerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderToFulfillmentXsltTest {

    private XsltTransformerService xsltTransformerService;

    private static final String SAMPLE_ORDER_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <orderRequest>
                <orderId>TEST-ORDER-001</orderId>
                <customerId>C-001</customerId>
                <customerEmail>test@example.com</customerEmail>
                <items>
                    <item><sku>SKU-LAPTOP</sku><qty>1</qty><price>999.99</price></item>
                    <item><sku>SKU-MOUSE</sku><qty>2</qty><price>29.99</price></item>
                </items>
                <shippingAddress>
                    <street>123 Main Street</street>
                    <city>Bucharest</city>
                    <zip>010101</zip>
                </shippingAddress>
                <paymentStatus>APPROVED</paymentStatus>
                <timestamp>2026-06-13T10:00:00Z</timestamp>
            </orderRequest>
            """;

    @BeforeEach
    void setUp() {
        xsltTransformerService = new XsltTransformerService();
    }

    @Test
    void transform_producesWellFormedFulfillmentWorkItem() {
        String result = xsltTransformerService.transform(SAMPLE_ORDER_XML, "xslt/order-to-fulfillment.xsl");

        assertThat(result).contains("<fulfillmentWorkItem>");
        assertThat(result).contains("<orderId>TEST-ORDER-001</orderId>");
    }

    @Test
    void transform_includesAllItemsFromOrderRequest() {
        String result = xsltTransformerService.transform(SAMPLE_ORDER_XML, "xslt/order-to-fulfillment.xsl");

        assertThat(result).contains("<sku>SKU-LAPTOP</sku>");
        assertThat(result).contains("<qty>1</qty>");
        assertThat(result).contains("<sku>SKU-MOUSE</sku>");
        assertThat(result).contains("<qty>2</qty>");
    }

    @Test
    void transform_includesShippingAddress() {
        String result = xsltTransformerService.transform(SAMPLE_ORDER_XML, "xslt/order-to-fulfillment.xsl");

        assertThat(result).contains("<street>123 Main Street</street>");
        assertThat(result).contains("<city>Bucharest</city>");
        assertThat(result).contains("<zip>010101</zip>");
    }

    @Test
    void transform_dropsConfidentialAndBusinessFields() {
        String result = xsltTransformerService.transform(SAMPLE_ORDER_XML, "xslt/order-to-fulfillment.xsl");

        assertThat(result).doesNotContain("<customerEmail>");
        assertThat(result).doesNotContain("<customerId>");
        assertThat(result).doesNotContain("<paymentStatus>");
        assertThat(result).doesNotContain("<timestamp>");
    }
}
