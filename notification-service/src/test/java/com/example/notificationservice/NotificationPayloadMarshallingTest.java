package com.example.notificationservice;

import com.example.notificationservice.model.NotificationPayload;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationPayloadMarshallingTest {

    private JAXBContext jaxbContext;

    @BeforeEach
    void setUp() throws Exception {
        jaxbContext = JAXBContext.newInstance(NotificationPayload.class);
    }

    @Test
    void marshalNotificationPayload_producesExpectedXmlElements() throws Exception {
        NotificationPayload payload = buildSamplePayload();

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter writer = new StringWriter();
        marshaller.marshal(payload, writer);
        String xml = writer.toString();

        assertThat(xml).contains("<notificationPayload>");
        assertThat(xml).contains("<recipientEmail>diana@example.com</recipientEmail>");
        assertThat(xml).contains("<subject>Your order ORDER-001 has shipped!</subject>");
        assertThat(xml).contains("<trackingUrl>https://track.example.com/TRK-ORDER-001</trackingUrl>");
        assertThat(xml).contains("<orderSummary>");
    }

    @Test
    void unmarshalNotificationPayload_roundTrip_preservesAllFields() throws Exception {
        NotificationPayload original = buildSamplePayload();

        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter writer = new StringWriter();
        marshaller.marshal(original, writer);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        NotificationPayload restored = (NotificationPayload) unmarshaller.unmarshal(new StringReader(writer.toString()));

        assertThat(restored.getRecipientEmail()).isEqualTo("diana@example.com");
        assertThat(restored.getSubject()).isEqualTo("Your order ORDER-001 has shipped!");
        assertThat(restored.getTrackingUrl()).isEqualTo("https://track.example.com/TRK-ORDER-001");
        assertThat(restored.getOrderSummary()).contains("WH-BUC-01");
    }

    private NotificationPayload buildSamplePayload() {
        NotificationPayload payload = new NotificationPayload();
        payload.setRecipientEmail("diana@example.com");
        payload.setSubject("Your order ORDER-001 has shipped!");
        payload.setOrderSummary("Order ORDER-001 dispatched from WH-BUC-01 on 2026-06-13T10:01:30Z. Estimated delivery: 2026-06-16.");
        payload.setTrackingUrl("https://track.example.com/TRK-ORDER-001");
        return payload;
    }
}
