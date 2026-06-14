package ro.ase.csie.fulfillmentservice;

import ro.ase.csie.fulfillmentservice.model.FulfillmentEvent;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FulfillmentEventMarshallingTest {

    private JAXBContext jaxbContext;

    @BeforeEach
    void setUp() throws Exception {
        jaxbContext = JAXBContext.newInstance(FulfillmentEvent.class);
    }

    @Test
    void marshalFulfillmentEvent_producesExpectedXmlElements() throws Exception {
        FulfillmentEvent event = buildSampleEvent();

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter writer = new StringWriter();
        marshaller.marshal(event, writer);
        String xml = writer.toString();

        assertThat(xml).contains("<fulfillmentEvent>");
        assertThat(xml).contains("<orderId>ORDER-001</orderId>");
        assertThat(xml).contains("<customerEmail>test@example.com</customerEmail>");
        assertThat(xml).contains("<warehouseId>WH-BUC-01</warehouseId>");
        assertThat(xml).contains("<trackingCode>TRK-ORDER-0</trackingCode>");
        assertThat(xml).contains("<estimatedDelivery>2026-06-16</estimatedDelivery>");
    }

    @Test
    void unmarshalFulfillmentEvent_roundTrip_preservesAllFields() throws Exception {
        FulfillmentEvent original = buildSampleEvent();

        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter writer = new StringWriter();
        marshaller.marshal(original, writer);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        FulfillmentEvent restored = (FulfillmentEvent) unmarshaller.unmarshal(new StringReader(writer.toString()));

        assertThat(restored.getOrderId()).isEqualTo("ORDER-001");
        assertThat(restored.getCustomerEmail()).isEqualTo("test@example.com");
        assertThat(restored.getWarehouseId()).isEqualTo("WH-BUC-01");
        assertThat(restored.getTrackingCode()).isEqualTo("TRK-ORDER-0");
        assertThat(restored.getDispatchTimestamp()).isEqualTo(Instant.parse("2026-06-13T10:01:30Z"));
        assertThat(restored.getEstimatedDelivery()).isEqualTo(LocalDate.parse("2026-06-16"));
    }

    private FulfillmentEvent buildSampleEvent() {
        FulfillmentEvent event = new FulfillmentEvent();
        event.setOrderId("ORDER-001");
        event.setCustomerEmail("test@example.com");
        event.setWarehouseId("WH-BUC-01");
        event.setDispatchTimestamp(Instant.parse("2026-06-13T10:01:30Z"));
        event.setTrackingCode("TRK-ORDER-0");
        event.setEstimatedDelivery(LocalDate.parse("2026-06-16"));
        return event;
    }
}
