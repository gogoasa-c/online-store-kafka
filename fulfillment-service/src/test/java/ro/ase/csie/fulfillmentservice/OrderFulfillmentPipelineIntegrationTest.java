package ro.ase.csie.fulfillmentservice;

import ro.ase.csie.shared.models.Item;
import ro.ase.csie.shared.models.OrderRequest;
import ro.ase.csie.shared.models.ShippingAddress;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EmbeddedKafka(
        partitions = 1,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers",
        topics = {"order.placed", "order.fulfilled"}
)
class OrderFulfillmentPipelineIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Test
    void orderPlaced_fulfillmentEventPublishedToOutputTopic() throws Exception {
        String orderId = "inttest-orderid01";
        String orderXml = buildOrderXml(orderId);

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "inttest-verifier");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        DefaultKafkaConsumerFactory<String, String> factory = new DefaultKafkaConsumerFactory<>(consumerProps);
        Consumer<String, String> verifier = factory.createConsumer();
        verifier.subscribe(List.of("order.fulfilled"));

        kafkaTemplate.send("order.placed", orderId, orderXml);

        ConsumerRecords<String, String> records = ConsumerRecords.empty();
        long deadline = System.currentTimeMillis() + 15_000;
        while (records.isEmpty() && System.currentTimeMillis() < deadline) {
            records = verifier.poll(Duration.ofMillis(500));
        }
        verifier.close();

        assertThat(records.count()).isGreaterThan(0);
        String fulfillmentXml = records.iterator().next().value();
        assertThat(fulfillmentXml)
                .contains("<fulfillmentEvent>")
                .contains("<orderId>" + orderId + "</orderId>")
                .contains("<warehouseId>WH-BUC-01</warehouseId>")
                .contains("<customerEmail>inttest@example.com</customerEmail>");
    }

    private String buildOrderXml(String orderId) throws JAXBException {
        OrderRequest order = new OrderRequest();
        order.setOrderId(orderId);
        order.setCustomerEmail("inttest@example.com");
        order.setCustomerId("C-INTTEST");
        order.setTimestamp(Instant.now());
        order.setItems(List.of(new Item("SKU-TEST", 1, 9.99)));

        ShippingAddress address = new ShippingAddress();
        address.setStreet("Test Street 1");
        address.setCity("Bucharest");
        address.setZip("010101");
        order.setShippingAddress(address);

        JAXBContext ctx = JAXBContext.newInstance(OrderRequest.class);
        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter sw = new StringWriter();
        m.marshal(order, sw);
        return sw.toString();
    }
}
