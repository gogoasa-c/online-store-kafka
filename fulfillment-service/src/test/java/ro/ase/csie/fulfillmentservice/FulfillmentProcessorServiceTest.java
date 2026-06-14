package ro.ase.csie.fulfillmentservice;

import ro.ase.csie.shared.models.OrderRequest;
import ro.ase.csie.fulfillmentservice.service.FulfillmentProcessorService;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FulfillmentProcessorServiceTest {

    private FulfillmentProcessorService service;

    @BeforeEach
    void setUp() throws JAXBException {
        service = new FulfillmentProcessorService("WH-BUC-01", 3);
    }

    private static OrderRequest orderWith(String orderId) {
        OrderRequest order = new OrderRequest();
        order.setOrderId(orderId);
        order.setCustomerEmail("customer@example.com");
        return order;
    }

    @Test
    void process_producesXmlWithFulfillmentEventRootElement() throws Exception {
        String xml = service.process(orderWith("12345678-abcd-1234-efgh-000000000000"));
        assertThat(xml).contains("<fulfillmentEvent>");
    }

    @Test
    void process_setsWarehouseIdToWH_BUC_01() throws Exception {
        String xml = service.process(orderWith("12345678-abcd-1234-efgh-000000000000"));
        assertThat(xml).contains("<warehouseId>WH-BUC-01</warehouseId>");
    }

    @Test
    void process_trackingCodeContainsFirst8CharsOfOrderIdUpperCased() throws Exception {
        String xml = service.process(orderWith("abcdef12-1234-5678-abcd-000000000000"));
        assertThat(xml).contains("<trackingCode>TRK-ABCDEF12</trackingCode>");
    }

    @Test
    void process_estimatedDeliveryIsThreeDaysFromToday() throws Exception {
        String xml = service.process(orderWith("12345678-abcd-1234-efgh-000000000000"));
        String expected = LocalDate.now().plusDays(3).toString();
        assertThat(xml).contains("<estimatedDelivery>" + expected + "</estimatedDelivery>");
    }
}
