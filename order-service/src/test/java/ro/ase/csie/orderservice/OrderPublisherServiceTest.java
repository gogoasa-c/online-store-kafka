package ro.ase.csie.orderservice;

import ro.ase.csie.orderservice.dto.CreateOrderDto;
import ro.ase.csie.orderservice.dto.ItemDto;
import ro.ase.csie.orderservice.dto.ShippingAddressDto;
import ro.ase.csie.orderservice.service.OrderPublisherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPublisherServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private OrderPublisherService service;

    @Test
    void publishOrder_sendsXmlToOrderPlacedTopic() throws Exception {
        when(kafkaTemplate.send(any(String.class), any(String.class), any(String.class)))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));

        service = new OrderPublisherService(kafkaTemplate);

        CreateOrderDto dto = new CreateOrderDto(
                "C-001",
                "test@example.com",
                List.of(new ItemDto("SKU-A", 2, 19.99)),
                new ShippingAddressDto("123 Main St", "Bucharest", "010101")
        );

        String orderId = service.publishOrder(dto);

        assertThat(orderId).isNotBlank();

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("order.placed");
        assertThat(keyCaptor.getValue()).isEqualTo(orderId);

        String xml = valueCaptor.getValue();
        assertThat(xml).contains("<orderRequest>");
        assertThat(xml).contains("<orderId>" + orderId + "</orderId>");
        assertThat(xml).contains("<customerEmail>test@example.com</customerEmail>");
        assertThat(xml).contains("<sku>SKU-A</sku>");
        assertThat(xml).contains("<paymentStatus>PENDING</paymentStatus>");
    }
}
