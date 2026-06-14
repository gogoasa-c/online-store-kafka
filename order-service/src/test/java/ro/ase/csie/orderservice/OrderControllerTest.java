package ro.ase.csie.orderservice;

import ro.ase.csie.orderservice.controller.OrderController;
import ro.ase.csie.orderservice.service.OrderPublisherService;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderPublisherService publisherService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(publisherService)).build();
    }

    private static final String VALID_ORDER = """
            {
                "customerId": "C-001",
                "customerEmail": "customer@example.com",
                "items": [{"sku": "SKU-A", "qty": 2, "price": 19.99}],
                "shippingAddress": {"street": "123 Main St", "city": "Bucharest", "zip": "010101"}
            }
            """;

    @Test
    void placeOrder_validRequest_returns202WithOrderIdAndStatus() throws Exception {
        when(publisherService.publishOrder(any())).thenReturn("test-order-uuid");

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ORDER))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderId").value("test-order-uuid"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void placeOrder_whenServiceThrowsJaxbException_returns500WithErrorMessage() throws Exception {
        when(publisherService.publishOrder(any())).thenThrow(new JAXBException("marshalling failed"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ORDER))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Failed to serialize order")));
    }
}
