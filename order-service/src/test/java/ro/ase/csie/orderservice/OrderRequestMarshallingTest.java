package ro.ase.csie.orderservice;

import ro.ase.csie.shared.models.Item;
import ro.ase.csie.shared.models.OrderRequest;
import ro.ase.csie.shared.models.ShippingAddress;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRequestMarshallingTest {

    private JAXBContext jaxbContext;

    @BeforeEach
    void setUp() throws Exception {
        jaxbContext = JAXBContext.newInstance(OrderRequest.class);
    }

    @Test
    void marshalOrderRequest_producesExpectedXmlElements() throws Exception {
        OrderRequest order = buildSampleOrder();

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter writer = new StringWriter();
        marshaller.marshal(order, writer);
        String xml = writer.toString();

        assertThat(xml).contains("<orderRequest>");
        assertThat(xml).contains("<orderId>ORDER-001</orderId>");
        assertThat(xml).contains("<customerId>C-001</customerId>");
        assertThat(xml).contains("<customerEmail>test@example.com</customerEmail>");
        assertThat(xml).contains("<items>");
        assertThat(xml).contains("<item>");
        assertThat(xml).contains("<sku>SKU-A</sku>");
        assertThat(xml).contains("<qty>2</qty>");
        assertThat(xml).contains("<paymentStatus>APPROVED</paymentStatus>");
        assertThat(xml).contains("<street>123 Main St</street>");
    }

    @Test
    void unmarshalOrderRequest_roundTrip_preservesAllFields() throws Exception {
        OrderRequest original = buildSampleOrder();

        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter writer = new StringWriter();
        marshaller.marshal(original, writer);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        OrderRequest restored = (OrderRequest) unmarshaller.unmarshal(new StringReader(writer.toString()));

        assertThat(restored.getOrderId()).isEqualTo("ORDER-001");
        assertThat(restored.getCustomerId()).isEqualTo("C-001");
        assertThat(restored.getCustomerEmail()).isEqualTo("test@example.com");
        assertThat(restored.getPaymentStatus()).isEqualTo("APPROVED");
        assertThat(restored.getItems()).hasSize(1);
        assertThat(restored.getItems().get(0).getSku()).isEqualTo("SKU-A");
        assertThat(restored.getItems().get(0).getQty()).isEqualTo(2);
        assertThat(restored.getShippingAddress().getCity()).isEqualTo("Bucharest");
    }

    private OrderRequest buildSampleOrder() {
        OrderRequest order = new OrderRequest();
        order.setOrderId("ORDER-001");
        order.setCustomerId("C-001");
        order.setCustomerEmail("test@example.com");
        order.setPaymentStatus("APPROVED");
        order.setTimestamp(Instant.parse("2026-06-13T10:00:00Z"));
        order.setItems(List.of(new Item("SKU-A", 2, 19.99)));
        order.setShippingAddress(new ShippingAddress("123 Main St", "Bucharest", "010101"));
        return order;
    }
}
