package ro.ase.csie.fulfillmentservice.model;

import lombok.Setter;
import ro.ase.csie.fulfillmentservice.model.adapter.InstantAdapter;
import ro.ase.csie.fulfillmentservice.model.adapter.LocalDateAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "fulfillmentEvent")
@XmlAccessorType(XmlAccessType.FIELD)
public class FulfillmentEvent {

    private String orderId;
    private String customerEmail;
    private String warehouseId;

    @XmlJavaTypeAdapter(InstantAdapter.class)
    private Instant dispatchTimestamp;

    private String trackingCode;

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate estimatedDelivery;
}
