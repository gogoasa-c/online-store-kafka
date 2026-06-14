package ro.ase.csie.shared.models;

import ro.ase.csie.shared.adapter.InstantAdapter;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@XmlRootElement(name = "orderRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderRequest {

    private String orderId;
    private String customerId;
    private String customerEmail;

    @XmlElementWrapper(name = "items")
    @XmlElement(name = "item")
    private List<Item> items = new ArrayList<>();

    private ShippingAddress shippingAddress;
    private String paymentStatus;

    @XmlJavaTypeAdapter(InstantAdapter.class)
    private Instant timestamp;
}
